package com.pal.dipesh.razorpay.merchant.security.rsa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security.jwt")
public record RsaPrivateKeyProperties(
        @NotBlank String privateKey,
        @NotBlank String passphrase,
        @Positive long accessTokenExpiryMs,
        @Positive long refreshTokenExpiryMs,
        @NotBlank String issuer
) {
}
