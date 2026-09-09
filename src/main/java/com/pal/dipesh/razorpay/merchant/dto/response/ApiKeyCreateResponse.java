package com.pal.dipesh.razorpay.merchant.dto.response;

import com.pal.dipesh.razorpay.common.enums.Environment;

import java.util.UUID;

public record ApiKeyCreateResponse(
        UUID id,
        String keyId,
        String keySecret,
        Environment environment
) {
}