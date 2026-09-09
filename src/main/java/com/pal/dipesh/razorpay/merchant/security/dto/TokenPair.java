package com.pal.dipesh.razorpay.merchant.security.dto;

import java.time.Instant;

public record TokenPair(
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt
) {
}
