package com.pal.dipesh.razorpay.merchant.dto.response;

import com.pal.dipesh.razorpay.common.enums.TokenType;

public record TokenRefreshResponse(
        String accessToken,
        TokenType tokenType,
        long accessTokenExpiresInSeconds,
        long refreshTokenExpiresInSeconds
) {
}
