package com.pal.dipesh.razorpay.merchant.security.dto;

import java.time.Instant;

public record AccessTokenClaims(
        String jti,
        String subject,
        Instant expiry,
        String rti
) {
}
