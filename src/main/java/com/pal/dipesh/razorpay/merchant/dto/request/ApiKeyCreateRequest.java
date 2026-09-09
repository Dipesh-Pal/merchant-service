package com.pal.dipesh.razorpay.merchant.dto.request;

import com.pal.dipesh.razorpay.common.enums.Environment;

public record ApiKeyCreateRequest(
        Environment environment
) {
}