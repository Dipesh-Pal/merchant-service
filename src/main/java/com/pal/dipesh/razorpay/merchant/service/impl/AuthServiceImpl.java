package com.pal.dipesh.razorpay.merchant.service.impl;

import com.pal.dipesh.razorpay.common.enums.UserRole;
import com.pal.dipesh.razorpay.common.exception.BusinessRuleViolationException;
import com.pal.dipesh.razorpay.common.exception.DuplicateResourceException;
import com.pal.dipesh.razorpay.common.exception.ResourceNotFoundException;
import com.pal.dipesh.razorpay.merchant.security.dto.AccessTokenClaims;
import com.pal.dipesh.razorpay.merchant.dto.request.LoginRequest;
import com.pal.dipesh.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.pal.dipesh.razorpay.merchant.dto.response.MerchantResponse;
import com.pal.dipesh.razorpay.merchant.entity.AppUser;
import com.pal.dipesh.razorpay.merchant.entity.Merchant;
import com.pal.dipesh.razorpay.merchant.mapper.MerchantMapper;
import com.pal.dipesh.razorpay.merchant.repository.AppUserRepository;
import com.pal.dipesh.razorpay.merchant.repository.MerchantRepository;
import com.pal.dipesh.razorpay.merchant.security.util.JwtUtil;
import com.pal.dipesh.razorpay.common.blocklist.TokenBlockListService;
import com.pal.dipesh.razorpay.merchant.security.dto.RefreshTokenClaims;
import com.pal.dipesh.razorpay.merchant.security.dto.TokenPair;
import com.pal.dipesh.razorpay.merchant.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final Duration REFRESH_BLOCKLIST_FALLBACK_TTL = Duration.ofHours(24);

    private final TokenBlockListService blockListService;
    private final MerchantRepository merchantRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final MerchantMapper merchantMapper;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public MerchantResponse signup(MerchantSignupRequest request) {
        if(merchantRepository.existsByEmail(request.email())) {
            log.warn("Merchant with email {} already exists", request.email());
            throw new DuplicateResourceException("DUPLICATE_MERCHANT_EMAIL", "Merchant with email " + request.email() + " already exists");
        }

        Merchant merchant = merchantMapper.toEntity(request);

        merchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser.builder()
                .email(request.email())
                .merchant(merchant)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.OWNER)
                .build();

        appUserRepository.save(appUser);

        return merchantMapper.toMerchantResponse(merchant);
    }

    @Override
    public AuthResult login(LoginRequest loginRequest) {
        AppUser appUser = appUserRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", loginRequest.email()));

        String email = appUser.getEmail();
        UUID merchantId = appUser.getMerchant().getId();
        UserRole role = appUser.getRole();

        if(!passwordEncoder.matches(loginRequest.password(), appUser.getPasswordHash())) {
            log.warn("Invalid password for user {}", email);
            throw new BusinessRuleViolationException("INVALID_CREDENTIALS", "Invalid email or password");
        }

        TokenPair tokens = jwtUtil.generateTokenPair(email, merchantId, role);

        return new AuthResult(tokens, email, merchantId, role);
    }

    @Override
    public TokenPair refreshTokens(AccessTokenClaims accessTokenClaims, RefreshTokenClaims refreshTokenClaims) {
        if(refreshTokenClaims == null) {
            throw new BusinessRuleViolationException("INVALID_CREDENTIALS", "Refresh token is required for refreshing access token");
        }

        String email = refreshTokenClaims.subject();

        AppUser user = appUserRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("AppUser", email));

        UUID merchantId = user.getMerchant().getId();
        UserRole freshRole = user.getRole();

        TokenPair newPair = jwtUtil.generateTokenPair(email, merchantId, freshRole);

        // Revoke the pair that was just presented so the same refresh can't be replayed.
        invalidateTokens(accessTokenClaims, refreshTokenClaims);

        return newPair;
    }

    @Override
    public void invalidateTokens(AccessTokenClaims accessTokenClaims, RefreshTokenClaims refreshTokenClaims) {
        String accessJti = accessTokenClaims == null ? null : accessTokenClaims.jti();
        Instant accessExp = accessTokenClaims == null ? null : accessTokenClaims.expiry();
        String accessSub = accessTokenClaims == null ? null : accessTokenClaims.subject();
        String rti = accessTokenClaims == null ? null : accessTokenClaims.rti();

        Instant primaryRefreshExp = rti == null ? null : Instant.now().plus(REFRESH_BLOCKLIST_FALLBACK_TTL);

        String secondaryRefreshJti = null;
        Instant secondaryRefreshExp = null;

        if (refreshTokenClaims != null) {
            String cookieJti = refreshTokenClaims.jti();
            String cookieSub = refreshTokenClaims.subject();
            Instant cookieExp = refreshTokenClaims.expiry();

            if (!Objects.equals(cookieSub, accessSub)) {
                log.warn("Logout token-mixing detected: accessSub={} cookieSub={} — ignoring cookie", accessSub, cookieSub);
            } else if (Objects.equals(cookieJti, rti)) {
                primaryRefreshExp = cookieExp;
            } else {
                // Same user, different session — blocklist both so neither can be reused.
                log.warn("Logout session mismatch for sub={}: accessRti={} cookieJti={} — blocklisting both", accessSub, rti, cookieJti);
                secondaryRefreshJti = cookieJti;
                secondaryRefreshExp = cookieExp;
            }
        }

        if (rti == null && secondaryRefreshJti == null) {
            log.warn("Access token has no rti claim and no refresh cookie present — refresh side not blocklisted (accessSub={})", accessSub);
        }

        blockListService.blockListPair(accessJti, accessExp, rti, primaryRefreshExp);

        if (secondaryRefreshJti != null) {
            blockListService.blockList(secondaryRefreshJti, secondaryRefreshExp);
        }
    }
}
