package com.pal.dipesh.razorpay.merchant.service.impl;

import com.pal.dipesh.razorpay.common.exception.ResourceNotFoundException;
import com.pal.dipesh.razorpay.merchant.dto.response.AppUserResponse;
import com.pal.dipesh.razorpay.merchant.repository.AppUserRepository;
import com.pal.dipesh.razorpay.merchant.service.AppUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;

    @Override
    public AppUserResponse getAppUserByEmail(String username) {
        return appUserRepository.findByEmail(username)
                .map(appUser -> new AppUserResponse(
                        appUser.getId(),
                        appUser.getEmail(),
                        appUser.getMerchant().getId(),
                        appUser.getMerchant().getBusinessName()
                ))
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", username));
    }
}
