package com.pal.dipesh.razorpay.merchant.security.rsa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RsaKeyConfig {

    private final RsaPrivateKeyProperties rsaPrivateKeyProperties;

    @Bean
    public RSAPrivateKey rsaPrivateKey() throws Exception {
        // Step 1: decode outer Base64 layer to get PEM string
        String pem = new String(Base64.getDecoder().decode(rsaPrivateKeyProperties.privateKey()))
                .replace("-----BEGIN ENCRYPTED PRIVATE KEY-----", "")
                .replace("-----END ENCRYPTED PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        // Step 2: decode inner Base64 layer to get encrypted DER bytes
        byte[] encryptedDer = Base64.getDecoder().decode(pem);

        // Step 3: parse encryption metadata (algorithm, IV, salt)
        EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(encryptedDer);

        // Step 4: build PBE key from passphrase
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(encryptedPrivateKeyInfo.getAlgName());
        PBEKeySpec pbeKeySpec = new PBEKeySpec(rsaPrivateKeyProperties.passphrase().toCharArray());
        SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);

        // Step 5: decrypt the DER bytes
        Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, secretKey, encryptedPrivateKeyInfo.getAlgParameters());
        PKCS8EncodedKeySpec keySpec = encryptedPrivateKeyInfo.getKeySpec(cipher);

        // Step 6: clear passphrase from memory immediately
        pbeKeySpec.clearPassword();

        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }
}
