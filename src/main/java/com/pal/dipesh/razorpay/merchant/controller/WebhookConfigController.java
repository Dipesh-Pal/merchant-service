package com.pal.dipesh.razorpay.merchant.controller;

import com.pal.dipesh.razorpay.common.context.CustomRequestContext;
import com.pal.dipesh.razorpay.merchant.dto.request.UpdateWebhookConfigRequest;
import com.pal.dipesh.razorpay.merchant.dto.response.WebhookConfigResponse;
import com.pal.dipesh.razorpay.merchant.service.WebhookConfigService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/merchants/webhooks")
public class WebhookConfigController {

    private final WebhookConfigService webhookConfigService;
    private final CustomRequestContext customRequestContext;

    @PostMapping
    public ResponseEntity<WebhookConfigResponse> create(@Valid @RequestBody UpdateWebhookConfigRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(webhookConfigService.create(customRequestContext.getMerchantId(), request));
    }

    @GetMapping
    public ResponseEntity<List<WebhookConfigResponse>> list() {
        return ResponseEntity.ok(webhookConfigService.list(customRequestContext.getMerchantId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WebhookConfigResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(webhookConfigService.getById(customRequestContext.getMerchantId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WebhookConfigResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateWebhookConfigRequest request) {
        return ResponseEntity.ok(webhookConfigService.update(customRequestContext.getMerchantId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        webhookConfigService.delete(customRequestContext.getMerchantId(), id);
        return ResponseEntity.noContent().build();
    }
}
