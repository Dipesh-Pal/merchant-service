package com.pal.dipesh.razorpay.merchant.service.impl;

import com.pal.dipesh.razorpay.common.enums.MerchantStatus;
import com.pal.dipesh.razorpay.common.exception.ResourceNotFoundException;
import com.pal.dipesh.razorpay.common.pojo.SettlementBankingDetails;
import com.pal.dipesh.razorpay.common.pojo.WebhookTarget;
import com.pal.dipesh.razorpay.merchant.api.MerchantLookupService;
import com.pal.dipesh.razorpay.merchant.entity.Merchant;
import com.pal.dipesh.razorpay.merchant.repository.MerchantRepository;
import com.pal.dipesh.razorpay.merchant.repository.WebhookConfigRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantLookupServiceImpl implements MerchantLookupService {

    private final WebhookConfigRepository webhookConfigRepository;
    private final MerchantRepository merchantRepository;
    private final BytesEncryptor masterKeyEncryptor;

    @Override
    public List<WebhookTarget> getActiveConfigsForEvent(UUID merchantId, String eventType) {
        return webhookConfigRepository.findByMerchant_IdAndEnabledTrue(merchantId).stream()
                .filter(config -> config.isSubscribedTo(eventType))
                .map(config -> {
                    byte[] decryptedSecretBytes = masterKeyEncryptor.decrypt(Base64.getDecoder().decode(config.getWebhookSecret()));

                    return new WebhookTarget(
                            config.getId(),
                            config.getTargetUrl(),
                            new String(decryptedSecretBytes, StandardCharsets.UTF_8)
                    );
                })
                .toList();
    }

    @Override
    public List<UUID> getAllActiveMerchantIds() {
        return merchantRepository.findByStatus(MerchantStatus.ACTIVE).stream()
                .map(Merchant::getId)
                .toList();
    }

    @Override
    public SettlementBankingDetails getSettlementBankingDetails(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", merchantId));

        return new SettlementBankingDetails(
                merchant.getSettlementBankAccountNumber(),
                merchant.getSettlementBankAccountIfsc(),
                merchant.getSettlementBankAccountHolderName()
        );
    }
}
