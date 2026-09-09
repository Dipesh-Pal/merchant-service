package com.pal.dipesh.razorpay.merchant.dto.response;

import com.pal.dipesh.razorpay.common.enums.TokenType;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        TokenType tokenType,
        long accessTokenExpiresInSeconds,
        long refreshTokenExpiresInSeconds,
        UUID merchantId,
        String email,
        String role
) {
}
