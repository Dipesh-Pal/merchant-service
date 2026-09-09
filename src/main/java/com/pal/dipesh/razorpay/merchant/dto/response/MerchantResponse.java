package com.pal.dipesh.razorpay.merchant.dto.response;

import com.pal.dipesh.razorpay.common.enums.BusinessType;
import com.pal.dipesh.razorpay.common.enums.MerchantStatus;

import java.util.UUID;

public record MerchantResponse(
        UUID id,
        String name,
        String email,
        String businessName,
        BusinessType businessType,
        MerchantStatus merchantStatus
) {
}