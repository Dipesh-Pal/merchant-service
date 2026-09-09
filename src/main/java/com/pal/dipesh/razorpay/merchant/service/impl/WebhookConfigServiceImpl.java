package com.pal.dipesh.razorpay.merchant.service.impl;

import com.pal.dipesh.razorpay.common.exception.ResourceNotFoundException;
import com.pal.dipesh.razorpay.common.util.RandomizerUtil;
import com.pal.dipesh.razorpay.merchant.dto.request.UpdateWebhookConfigRequest;
import com.pal.dipesh.razorpay.merchant.dto.response.WebhookConfigResponse;
import com.pal.dipesh.razorpay.merchant.entity.Merchant;
import com.pal.dipesh.razorpay.merchant.entity.MerchantWebhookConfig;
import com.pal.dipesh.razorpay.merchant.mapper.WebhookConfigMapper;
import com.pal.dipesh.razorpay.merchant.repository.MerchantRepository;
import com.pal.dipesh.razorpay.merchant.repository.WebhookConfigRepository;
import com.pal.dipesh.razorpay.merchant.service.WebhookConfigService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebhookConfigServiceImpl implements WebhookConfigService {

    private final WebhookConfigRepository webhookConfigRepository;
    private final WebhookConfigMapper webhookConfigMapper;
    private final MerchantRepository merchantRepository;
    private final BytesEncryptor masterKeyEncryptor;

    @Override
    @Transactional
    public WebhookConfigResponse create(UUID merchantId, UpdateWebhookConfigRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() -> new ResourceNotFoundException("Merchant", merchantId));

        String rawSecret = RandomizerUtil.randomBase64(24);
        byte[] rawSecretBytes = rawSecret.getBytes(StandardCharsets.UTF_8);
        String encryptedSecret = Base64.getEncoder().encodeToString(masterKeyEncryptor.encrypt(rawSecretBytes));

        MerchantWebhookConfig config = MerchantWebhookConfig.builder()
                .merchant(merchant)
                .targetUrl(request.targetUrl())
                .enabled(true)
                .eventTypes(request.eventTypes())
                .webhookSecret(encryptedSecret)
                .build();

        config = webhookConfigRepository.save(config);

        return webhookConfigMapper.toResponse(config, rawSecret);
    }

    @Override
    public WebhookConfigResponse getById(UUID merchantId, UUID configId) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);

        return webhookConfigMapper.toResponse(config, null);
    }

    @Override
    public List<WebhookConfigResponse> list(UUID merchantId) {
        return webhookConfigRepository.findByMerchant_Id(merchantId).stream()
                .map(config -> webhookConfigMapper.toResponse(config, null))
                .toList();
    }

    @Override
    @Transactional
    public WebhookConfigResponse update(UUID merchantId, UUID configId, UpdateWebhookConfigRequest request) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
        config.setTargetUrl(request.targetUrl());
        config.setEventTypes(request.eventTypes());

        log.info("Merchant webhook config updated id={} merchantId={}", configId, merchantId);
        config = webhookConfigRepository.save(config);

        return webhookConfigMapper.toResponse(config, null);
    }

    @Override
    @Transactional
    public void delete(UUID merchantId, UUID configId) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
        webhookConfigRepository.delete(config);

        log.info("Merchant webhook config deleted id={} merchantId={}", configId, merchantId);
    }

    private MerchantWebhookConfig requireOwnedConfig(UUID merchantId, UUID configId) {
        return webhookConfigRepository.findByIdAndMerchant_Id(configId, merchantId).orElseThrow(() -> new ResourceNotFoundException("MerchantWebhookConfig", configId));
    }
}
