package com.pal.dipesh.razorpay.merchant.controller;

import com.pal.dipesh.razorpay.common.pojo.SettlementBankingDetails;
import com.pal.dipesh.razorpay.common.pojo.WebhookTarget;
import com.pal.dipesh.razorpay.merchant.api.MerchantLookupService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Service-to-service lookup API backing
 * {@code operations-service}'s {@code MerchantServiceClient} Feign interface.
 *
 * <p>Responses are deliberately <strong>not</strong> wrapped in
 * {@code ApiResponse}: {@code GlobalResponseHandler} skips any path under
 * {@code /internal/}, so the JSON on the wire matches the bare DTO return
 * types the Feign client declares.
 *
 * <p>Not part of the public merchant API - these routes bypass the JWT and
 * API-key filters chains and must not be exposed outside the cluster.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/merchants")
public class InternalMerchantController {

    private final MerchantLookupService merchantLookupService;

    /**
     * Returns the merchant's enabled webhook configs that subscribe to the
     * given event type, each with its decrypted signing secret.
     *
     * @param merchantId owning merchant
     * @param eventType  event type to filters subscriptions by
     * @return matching targets, empty if none are subscribed
     */
    @GetMapping("/{merchantId}/webhook-targets")
    public ResponseEntity<List<WebhookTarget>> getActiveConfigsForEvent(@PathVariable UUID merchantId, @RequestParam String eventType) {
        return ResponseEntity.ok(merchantLookupService.getActiveConfigsForEvent(merchantId, eventType));
    }

    /**
     * Returns the ids of every merchant in {@code ACTIVE} status, used to drive
     * settlement batching.
     */
    @GetMapping("/active-ids")
    public ResponseEntity<List<UUID>> getActiveMerchants() {
        return ResponseEntity.ok(merchantLookupService.getAllActiveMerchantIds());
    }

    /**
     * Returns the merchant's settlement bank account details.
     *
     * @param merchantId merchant to look up
     * @return the banking details
     * @throws com.pal.dipesh.razorpay.common.exception.ResourceNotFoundException
     *         if no merchant exists with that id (mapped to 404 by
     *         {@code GlobalExceptionHandler})
     */
    @GetMapping("/{merchantId}/settlement-bank-details")
    public ResponseEntity<SettlementBankingDetails> getSettlementBankDetails(@PathVariable UUID merchantId) {
        return ResponseEntity.ok(merchantLookupService.getSettlementBankingDetails(merchantId));
    }
}
