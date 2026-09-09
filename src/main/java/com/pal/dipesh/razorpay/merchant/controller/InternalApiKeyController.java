package com.pal.dipesh.razorpay.merchant.controller;

import com.pal.dipesh.razorpay.common.cache.ApiKeyCacheEntry;
import com.pal.dipesh.razorpay.merchant.service.ApiKeyService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/api-keys")
public class InternalApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping("/{keyId}")
    public ResponseEntity<ApiKeyCacheEntry> findByKeyId(@PathVariable String keyId) {
        ApiKeyCacheEntry apiKeyCacheEntry = apiKeyService.findByKeyId(keyId);
        return ResponseEntity.ok(apiKeyCacheEntry);
    }
}
