package com.pal.dipesh.razorpay.merchant.service.impl;

import com.pal.dipesh.razorpay.common.cache.ApiKeyCacheEntry;
import com.pal.dipesh.razorpay.common.context.CustomRequestContext;
import com.pal.dipesh.razorpay.common.exception.ApiKeyDisabledException;
import com.pal.dipesh.razorpay.common.exception.ResourceNotFoundException;
import com.pal.dipesh.razorpay.common.util.RandomizerUtil;
import com.pal.dipesh.razorpay.common.cache.ApiKeyCache;
import com.pal.dipesh.razorpay.merchant.dto.request.ApiKeyCreateRequest;
import com.pal.dipesh.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import com.pal.dipesh.razorpay.merchant.dto.response.ApiKeyResponse;
import com.pal.dipesh.razorpay.merchant.entity.ApiKey;
import com.pal.dipesh.razorpay.merchant.entity.Merchant;
import com.pal.dipesh.razorpay.merchant.mapper.ApiKeyMapper;
import com.pal.dipesh.razorpay.merchant.repository.ApiKeyRepository;
import com.pal.dipesh.razorpay.merchant.repository.MerchantRepository;
import com.pal.dipesh.razorpay.merchant.service.ApiKeyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiKeyServiceImpl implements ApiKeyService {

    private final CustomRequestContext customRequestContext;
    private final MerchantRepository merchantRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApiKeyMapper apiKeyMapper;
    private final ApiKeyCache apiKeyCache;

    @Override
    @Transactional
    public ApiKeyCreateResponse createApiKey(UUID merchantId, ApiKeyCreateRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() -> {
            log.warn("Merchant with id {} not found", merchantId);
            return new ResourceNotFoundException("merchant", merchantId);
        });

        String apiKeyId = "rzp_" + request.environment().name().toLowerCase() + "_" + RandomizerUtil.randomBase64(24);
        String rawSecret = RandomizerUtil.randomBase64(48);

        ApiKey apiKey = ApiKey.builder()
                .merchant(merchant)
                .keyId(apiKeyId)
                .keySecretHash(passwordEncoder.encode(rawSecret))
                .environment(request.environment())
                .build();

        apiKey = apiKeyRepository.save(apiKey);

        return apiKeyMapper.toApiKeyCreateResponse(apiKey, rawSecret);
    }

    @Override
    public List<ApiKeyResponse> listByMerchantId(UUID merchantId) {
        List<ApiKey> apiKeys = apiKeyRepository.findByMerchant_Id(merchantId).orElse(List.of());
        return apiKeys.isEmpty() ? List.of() : apiKeyMapper.toApiKeyResponse(apiKeys);
    }

    @Override
    @Transactional
    public void revoke(UUID merchantId, String keyId) {
        ApiKey apiKey = apiKeyRepository.findByKeyIdAndMerchant_Id(keyId, merchantId)
                .orElseThrow(() -> {
                    log.warn("Api key with keyId {} not found for merchant {}", keyId, merchantId);
                    return new ResourceNotFoundException("ApiKey", keyId);
                });

        apiKey.setEnabled(false);
        apiKey.setRevokedAt(LocalDateTime.now());
        apiKey.setRevokedBy(customRequestContext.getUsername());

        apiKeyCache.evict(apiKey.getKeyId());
        apiKeyRepository.save(apiKey);
    }

    @Override
    @Transactional
    public ApiKeyCreateResponse rotateKey(UUID merchantId, String keyId) {
        ApiKey apiKey = apiKeyRepository.findByKeyIdAndMerchant_Id(keyId, merchantId)
                .orElseThrow(() -> {
                    log.warn("ApiKey with keyId {} not found for merchant {}", keyId, merchantId);
                    return new ResourceNotFoundException("ApiKey", keyId);
                });

        if(!apiKey.isEnabled()){
           throw new ApiKeyDisabledException("API_KEY_DISABLED", "API key with id " + keyId + " is disabled and cannot be rotated");
        }

        String newRawSecret = RandomizerUtil.randomBase64(48);
        apiKey.setPreviousKeySecretHash(apiKey.getKeySecretHash());
        apiKey.setKeySecretHash(passwordEncoder.encode(newRawSecret));
        apiKey.setRotatedAt(LocalDateTime.now());
        apiKey.setRotatedBy(customRequestContext.getUsername());
        apiKey.setGracePeriodExpiresAt(LocalDateTime.now().plusHours(24)); // 24 hours grace period for old key to work

        apiKey = apiKeyRepository.save(apiKey);

        apiKeyCache.evict(apiKey.getKeyId());

        return apiKeyMapper.toApiKeyCreateResponse(apiKey, newRawSecret);
    }

    @Override
    public ApiKeyCacheEntry findByKeyId(String keyId) {
        ApiKey apiKey = apiKeyRepository.findByKeyId(keyId).orElseThrow(() -> {
            log.warn("ApiKey with keyId {} not found", keyId);
            return new ResourceNotFoundException("ApiKey", keyId);
        });

        return new ApiKeyCacheEntry(
                apiKey.getKeyId(),
                apiKey.getKeySecretHash(),
                apiKey.getPreviousKeySecretHash(),
                apiKey.getGracePeriodExpiresAt(),
                apiKey.getMerchant().getId(),
                apiKey.getEnvironment(),
                apiKey.isEnabled()
        );
    }
}