package com.pal.dipesh.razorpay.merchant.service;

import com.pal.dipesh.razorpay.merchant.dto.request.UpdateWebhookConfigRequest;
import com.pal.dipesh.razorpay.merchant.dto.response.WebhookConfigResponse;

import java.util.List;
import java.util.UUID;

public interface WebhookConfigService {
    WebhookConfigResponse create(UUID merchantId, UpdateWebhookConfigRequest request);

    WebhookConfigResponse getById(UUID merchantId, UUID configId);

    List<WebhookConfigResponse> list(UUID merchantId);

    WebhookConfigResponse update(UUID merchantId, UUID configId, UpdateWebhookConfigRequest request);

    void delete(UUID merchantId, UUID configId);
}
