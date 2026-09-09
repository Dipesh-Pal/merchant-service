package com.pal.dipesh.razorpay.merchant.entity;

import com.pal.dipesh.razorpay.common.pojo.BaseAuditEntity;

import jakarta.persistence.*;

import lombok.*;

/**
 * Per-merchant webhook delivery configuration: target URL, event filters,
 * and signing secret.
 *
 * <p>Extends {@link BaseAuditEntity} — and full auditing is <strong>strongly
 * justified</strong> here: changing {@link #targetUrl} or rotating
 * {@link #webhookSecret} are prime exfiltration vectors (an attacker who
 * repoints webhooks captures payment events). Knowing the last actor on this
 * row is a security control, not a nice-to-have.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "merchant_webhook_config",
        indexes = {
                @Index(name = "idx_mwc_merchant_id_enabled", columnList = "merchant_id, enabled")
        }
)
public class MerchantWebhookConfig extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "target_url", nullable = false, length = 500)
    private String targetUrl;

    @Column (name = "event_types", length = 200)
    private String eventTypes;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "webhook_secret_hash", nullable = false, length = 300)
    private String webhookSecret;

    public boolean isSubscribedTo(String eventType) {
        if (eventTypes == null || eventTypes.isBlank()) {
            return true;
        }

        for (String type : eventTypes.split(",")) {
            String trimmedType = type.trim();

            if (trimmedType.equalsIgnoreCase("ALL") || trimmedType.equalsIgnoreCase(eventType)) {
                return true;
            }
        }

        return false;
    }
}