package com.pal.dipesh.razorpay.merchant.entity;

import com.pal.dipesh.razorpay.common.enums.BusinessType;
import com.pal.dipesh.razorpay.common.enums.MerchantStatus;
import com.pal.dipesh.razorpay.common.pojo.BaseAuditEntity;

import jakarta.persistence.*;

import lombok.*;

/**
 * Top-level merchant aggregate — the tenancy boundary for almost every other
 * pojo in the system (most tables carry a {@code merchant_id} foreign key
 * or denormalized id back to this row).
 *
 * <p>Extends {@link BaseAuditEntity} because a merchant is a long-lived
 * <em>state-bag</em>: profile fields, KYC status, and bank-account details
 * are mutated over time by ops users and self-service flows. Knowing the
 * last actor on each row is a compliance signal worth preserving.
 *
 * <p>Lifecycle is driven by {@link MerchantStatus}; new rows default to
 * {@link MerchantStatus#PENDING_KYC}.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "merchant",
        indexes = {
                @Index(name = "idx_merchant_status", columnList = "status")
        }
)
@EqualsAndHashCode(callSuper = true)
public class Merchant extends BaseAuditEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "email", nullable = false, length = 80, unique = true)
    private String email;

    @Column(name = "contact_no", length = 20)
    private String contactNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false, length = 50)
    private BusinessType businessType;

    @Column(name = "business_name", nullable = false, length = 100)
    private String businessName;

    @Column(name = "website_url", length = 200)
    private String websiteUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 80)
    private MerchantStatus status = MerchantStatus.PENDING_KYC;

    @Column(name = "gst_id", length = 20)
    private String gstId;

    @Column(name = "pan_no", length = 20)
    private String panNumber;

    @Column(name = "settlement_bank_account_no", length = 80)
    private String settlementBankAccountNumber;

    @Column(name = "settlement_bank_account_ifsc", length = 20)
    private String settlementBankAccountIfsc;

    @Column(name = "settlement_bank_account_holder_name", length = 200)
    private String settlementBankAccountHolderName;
}