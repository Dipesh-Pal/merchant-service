package com.pal.dipesh.razorpay.merchant.controller;

import com.pal.dipesh.razorpay.common.annotation.ResponseMessage;
import com.pal.dipesh.razorpay.common.constants.CustomHeaders;
import com.pal.dipesh.razorpay.common.enums.TokenType;
import com.pal.dipesh.razorpay.common.util.CookieUtil;
import com.pal.dipesh.razorpay.merchant.dto.request.LoginRequest;
import com.pal.dipesh.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.pal.dipesh.razorpay.merchant.dto.response.LoginResponse;
import com.pal.dipesh.razorpay.merchant.dto.response.MerchantResponse;
import com.pal.dipesh.razorpay.merchant.dto.response.TokenRefreshResponse;
import com.pal.dipesh.razorpay.merchant.security.dto.AccessTokenClaims;
import com.pal.dipesh.razorpay.merchant.security.dto.RefreshTokenClaims;
import com.pal.dipesh.razorpay.merchant.security.dto.TokenPair;
import com.pal.dipesh.razorpay.merchant.service.AuthService;
import com.pal.dipesh.razorpay.merchant.service.AuthService.AuthResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/signup")
    @ResponseMessage("Merchant signup successful")
    public ResponseEntity<MerchantResponse> signup(@RequestBody @Valid MerchantSignupRequest merchantRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.signup(merchantRequest));
    }

    @PostMapping("/login")
    @ResponseMessage("Login successful")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        AuthResult result = authService.login(loginRequest);
        TokenPair tokens = result.tokens();

        ResponseCookie refreshCookie = cookieUtil.buildRefreshCookie(
                tokens.refreshToken(),
                tokens.refreshTokenExpiresAt()
        );

        Instant now = Instant.now();

        LoginResponse body = new LoginResponse(
                tokens.accessToken(),
                TokenType.ACCESS,
                secondsUntil(now, tokens.accessTokenExpiresAt()),
                secondsUntil(now, tokens.refreshTokenExpiresAt()),
                result.merchantId(),
                result.email(),
                result.role().name()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(body);
    }

    private static long secondsUntil(Instant now, Instant expiresAt) {
        return Duration.between(now, expiresAt).plusMillis(500).toSeconds();
    }

    @PostMapping("/refresh")
    @ResponseMessage("Access Token Refreshed")
    public ResponseEntity<TokenRefreshResponse> refreshToken(HttpServletRequest request) {
        AccessTokenClaims accessClaims = getAccessTokenClaims(request);
        RefreshTokenClaims refreshClaims = getRefreshTokenClaims(request);

        TokenPair tokens = authService.refreshTokens(accessClaims, refreshClaims);

        ResponseCookie refreshCookie = cookieUtil.buildRefreshCookie(
                tokens.refreshToken(),
                tokens.refreshTokenExpiresAt()
        );

        Instant now = Instant.now();

        TokenRefreshResponse body = new TokenRefreshResponse(
                tokens.accessToken(),
                TokenType.ACCESS,
                secondsUntil(now, tokens.accessTokenExpiresAt()),
                secondsUntil(now, tokens.refreshTokenExpiresAt())
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(body);
    }

    @PostMapping("/logout")
    @ResponseMessage("Logged Out Successfully")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.invalidateTokens(getAccessTokenClaims(request), getRefreshTokenClaims(request));

        ResponseCookie deleteCookie = cookieUtil.clearRefreshCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    private AccessTokenClaims getAccessTokenClaims(HttpServletRequest request) {
        String jti = header(request, CustomHeaders.ACCESS_TOKEN_JTI);

        if (jti == null) {
            return null;
        }

        return new AccessTokenClaims(
                jti,
                subject(request, CustomHeaders.ACCESS_TOKEN_SUBJECT),
                expiry(request, CustomHeaders.ACCESS_TOKEN_EXPIRES_AT),
                header(request, CustomHeaders.ACCESS_TOKEN_RTI)
        );
    }

    private RefreshTokenClaims getRefreshTokenClaims(HttpServletRequest request) {
        String jti = header(request, CustomHeaders.REFRESH_TOKEN_JTI);

        if (jti == null) {
            return null;
        }

        return new RefreshTokenClaims(
                jti,
                subject(request, CustomHeaders.REFRESH_TOKEN_SUBJECT),
                expiry(request, CustomHeaders.REFRESH_TOKEN_EXPIRES_AT)
        );
    }

    /**
     * The gateway only stamps a token-specific subject header for the token it validated
     * <em>in addition</em> to the one that authenticated the call: the refresh route omits
     * {@code X-Refresh-Token-Subject} and the logout route omits {@code X-Access-Token-Subject}.
     * In both cases {@code X-User-Id} carries the subject of the token it did authenticate, so it is
     * the correct fallback.
     *
     * <p>This matters: a null subject would make {@code refreshTokens} look the user up by
     * {@code null} and 404, and would make {@code invalidateTokens} read a genuine cookie as a
     * token-mixing attempt and skip blocklisting it.
     */
    private static String subject(HttpServletRequest request, String tokenSubjectHeader) {
        String tokenSubject = header(request, tokenSubjectHeader);

        return tokenSubject != null ? tokenSubject : header(request, CustomHeaders.USER_ID);
    }

    private static Instant expiry(HttpServletRequest request, String headerName) {
        String value = header(request, headerName);

        if (value == null) {
            return null;
        }

        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            log.warn("Unparseable {} header; that token will not be blocklisted", headerName, e);
            return null;
        }
    }

    private static String header(HttpServletRequest request, String name) {
        String value = request.getHeader(name);

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}