package com.pal.dipesh.razorpay.merchant.security.dto;

import java.time.Instant;

public record RefreshTokenClaims(
        String jti,
        String subject,
        Instant expiry
) {
}
