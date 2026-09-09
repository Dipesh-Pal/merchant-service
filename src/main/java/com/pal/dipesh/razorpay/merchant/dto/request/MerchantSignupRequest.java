package com.pal.dipesh.razorpay.merchant.dto.request;

import com.pal.dipesh.razorpay.common.enums.BusinessType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MerchantSignupRequest(

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @NotBlank(message = "Business Name is required")
        @Size(max = 50, message = "Business Name must be at most 50 characters long")
        String businessName,

        @NotNull(message = "Business type is required")
        BusinessType businessType
) {
}