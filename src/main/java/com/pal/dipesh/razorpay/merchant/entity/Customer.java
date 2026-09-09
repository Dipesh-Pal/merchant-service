package com.pal.dipesh.razorpay.merchant.entity;

import com.pal.dipesh.razorpay.common.pojo.BaseAuditEntity;

import jakarta.persistence.*;

import lombok.*;

/**
 * End customer of a {@link Merchant} (the buyer in a payment flow).
 *
 * <p>Extends {@link BaseAuditEntity} for uniformity. Contact details are
 * mutable (email/phone updates), so generic timestamp auditing is genuinely
 * useful; the principal columns will typically resolve to {@code "system"}
 * since customers are created through merchant API calls rather than human
 * actions.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "customer",
        indexes = {
                @Index(name = "idx_customer_merchant_id", columnList = "merchant_id"),
                @Index(name = "idx_customer_merchant_email", columnList = "merchant_id, email")
        }
)
@EqualsAndHashCode(callSuper = true)
public class Customer extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "email", nullable = false, length = 80)
    private String email;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "contact_no", length = 20)
    private String contactNumber;
}