package com.pal.dipesh.razorpay.merchant.entity;

import com.pal.dipesh.razorpay.common.enums.Environment;

import jakarta.persistence.*;

import lombok.*;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Credentials issued to a merchant.
 *
 * <p>Intentionally does <strong>not</strong> extend {@code BaseAuditEntity}:
 * <ul>
 *   <li>{@code updatedAt} / {@code updatedBy} would be churned on every API
 *       call (because {@link #lastUsedAt} is bumped per request) and would
 *       destroy the security audit trail.</li>
 *   <li>Lifecycle transitions are captured by dedicated, write-once
 *       timestamps: {@link #rotatedAt}, {@link #revokedAt},
 *       {@link #gracePeriodExpiresAt}.</li>
 * </ul>
 * Only the immutable creation pair ({@code createdAt}, {@code createdBy}) is
 * inherited from Spring Data JPA Auditing.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "api_key",
        indexes = {
                @Index(name = "idx_api_key_merchant_id_enabled", columnList = "merchant_id, enabled"),
                @Index(name = "idx_api_key_key_id", columnList = "key_id", unique = true)
        }
)
@EqualsAndHashCode(of = "id")
@EntityListeners(AuditingEntityListener.class)
public class ApiKey {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "key_id", nullable = false, length = 50)
    private String keyId;

    @Column(name = "previous_key_secret_hash", length = 200)
    private String previousKeySecretHash;

    @Column(name = "key_secret_hash", nullable = false, length = 200)
    private String keySecretHash;

    @Column(name = "web_hook_secret_hash", length = 200)
    private String webHookSecretHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false, length = 10)
    private Environment environment;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "rotated_at")
    private LocalDateTime rotatedAt;

    @Column(name = "grace_period_expires_at")
    private LocalDateTime gracePeriodExpiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "rotated_by")
    private String rotatedBy;

    @Column(name = "revoked_by")
    private String revokedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    public boolean isInGracePeriod() {
        return gracePeriodExpiresAt != null && LocalDateTime.now().isBefore(gracePeriodExpiresAt);
    }
}