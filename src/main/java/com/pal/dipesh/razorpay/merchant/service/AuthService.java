package com.pal.dipesh.razorpay.merchant.service;

import com.pal.dipesh.razorpay.common.enums.UserRole;
import com.pal.dipesh.razorpay.merchant.security.dto.AccessTokenClaims;
import com.pal.dipesh.razorpay.merchant.dto.request.LoginRequest;
import com.pal.dipesh.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.pal.dipesh.razorpay.merchant.dto.response.MerchantResponse;
import com.pal.dipesh.razorpay.merchant.security.dto.RefreshTokenClaims;
import com.pal.dipesh.razorpay.merchant.security.dto.TokenPair;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.UUID;

public interface AuthService {

    record AuthResult(
            TokenPair tokens,
            String email,
            UUID merchantId,
            UserRole role
    ) {}

    MerchantResponse signup(MerchantSignupRequest merchantRequest);

    AuthResult login(LoginRequest loginRequest);

    TokenPair refreshTokens(AccessTokenClaims accessTokenClaims, RefreshTokenClaims refreshTokenClaims);

    void invalidateTokens(AccessTokenClaims accessTokenClaims, RefreshTokenClaims refreshTokenClaims);
}

