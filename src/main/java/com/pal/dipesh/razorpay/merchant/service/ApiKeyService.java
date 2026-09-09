package com.pal.dipesh.razorpay.merchant.service;

import com.pal.dipesh.razorpay.common.cache.ApiKeyCacheEntry;
import com.pal.dipesh.razorpay.merchant.dto.request.ApiKeyCreateRequest;
import com.pal.dipesh.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import com.pal.dipesh.razorpay.merchant.dto.response.ApiKeyResponse;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {

    ApiKeyCreateResponse createApiKey(UUID merchantId, ApiKeyCreateRequest apiKeys);

    List<ApiKeyResponse> listByMerchantId(UUID merchantId);

    void revoke(UUID merchantId, String keyId);

    ApiKeyCreateResponse rotateKey(UUID merchantId, String keyId);

    ApiKeyCacheEntry findByKeyId(String keyId);
}
