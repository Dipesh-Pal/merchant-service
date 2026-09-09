package com.pal.dipesh.razorpay.merchant.dto.response;

import java.util.UUID;

public record AppUserResponse(
        UUID id,
        String username,
        UUID merchantId,
        String businessName
) {
}
