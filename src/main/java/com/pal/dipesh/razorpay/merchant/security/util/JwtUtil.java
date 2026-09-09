package com.pal.dipesh.razorpay.merchant.security.util;

import com.pal.dipesh.razorpay.common.enums.TokenType;
import com.pal.dipesh.razorpay.common.enums.UserRole;
import com.pal.dipesh.razorpay.merchant.security.rsa.RsaPrivateKeyProperties;
import com.pal.dipesh.razorpay.merchant.security.dto.TokenPair;

import io.jsonwebtoken.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final RsaPrivateKeyProperties rsaPrivateKeyProperties;
    private final RSAPrivateKey rsaPrivateKey;

    private record IssuedToken(String token, String jti, Instant expiresAt) {}

    /*----------------------- Token generation -----------------------*/

    public TokenPair generateTokenPair(String email, UUID merchantId, UserRole role) {
        IssuedToken refreshToken = buildRefreshToken(email, merchantId);
        IssuedToken accessToken  = buildAccessToken(email, merchantId, role, refreshToken.jti());

        return new TokenPair(
                accessToken.token(),
                refreshToken.token(),
                accessToken.expiresAt(),
                refreshToken.expiresAt()
        );
    }

    private IssuedToken buildAccessToken(String email, UUID merchantId, UserRole role, String refreshJti) {
        Instant now = Instant.now();
        Date exp = Date.from(now.plusMillis(rsaPrivateKeyProperties.accessTokenExpiryMs()));
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .header()
                .type("JWT")
                .keyId("rsa-key-1")
                .and()
                .id(jti)
                .subject(email)
                .issuer(rsaPrivateKeyProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(exp)
                .claim("merchant_id", merchantId.toString())
                .claim("role", role.name())
                .claim("type", TokenType.ACCESS.name())
                .claim("rti", refreshJti)               // paired refresh JTI — for logout revocation
                .signWith(rsaPrivateKey, Jwts.SIG.RS256)
                .compact();

        return new IssuedToken(token, jti, exp.toInstant());
    }

    private IssuedToken buildRefreshToken(String email, UUID merchantId) {
        Instant now = Instant.now();
        Date exp = Date.from(now.plusMillis(rsaPrivateKeyProperties.refreshTokenExpiryMs()));
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .header()
                .type("JWT")
                .keyId("rsa-key-1")
                .and()
                .id(jti)
                .subject(email)
                .issuer(rsaPrivateKeyProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(exp)
                .claim("merchant_id", merchantId.toString())
                .claim("type", TokenType.REFRESH.name())
                .signWith(rsaPrivateKey, Jwts.SIG.RS256)
                .compact();

        return new IssuedToken(token, jti, exp.toInstant());
    }
}
