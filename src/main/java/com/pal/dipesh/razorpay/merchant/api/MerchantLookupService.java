package com.pal.dipesh.razorpay.merchant.api;

import com.pal.dipesh.razorpay.common.pojo.SettlementBankingDetails;
import com.pal.dipesh.razorpay.common.pojo.WebhookTarget;

import java.util.List;
import java.util.UUID;

public interface MerchantLookupService {
    List<WebhookTarget> getActiveConfigsForEvent(UUID merchantId, String eventType);
    List<UUID> getAllActiveMerchantIds();
    SettlementBankingDetails getSettlementBankingDetails(UUID merchantId);
}
