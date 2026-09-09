package com.pal.dipesh.razorpay.merchant.controller;

import com.pal.dipesh.razorpay.common.annotation.ResponseMessage;
import com.pal.dipesh.razorpay.common.context.CustomRequestContext;
import com.pal.dipesh.razorpay.merchant.dto.response.AppUserResponse;
import com.pal.dipesh.razorpay.merchant.service.AppUserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/merchants/app-user")
public class AppUserController {

    private final AppUserService appUserService;
    private final CustomRequestContext customRequestContext;

    @GetMapping
    @ResponseMessage("User profile fetched")
    public ResponseEntity<AppUserResponse> getUser(){
        return ResponseEntity.ok(appUserService.getAppUserByEmail(customRequestContext.getUsername()));
    }
}
