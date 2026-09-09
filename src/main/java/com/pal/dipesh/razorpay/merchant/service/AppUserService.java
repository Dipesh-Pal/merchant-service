package com.pal.dipesh.razorpay.merchant.service;

import com.pal.dipesh.razorpay.merchant.dto.response.AppUserResponse;

public interface AppUserService {
    AppUserResponse getAppUserByEmail(String username);
}
