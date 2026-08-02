package com.hotelfo.scanner.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * JPA AttributeConverter yang mengenkripsi/mendekripsi kolom sensitif (passportNumber)
 * secara transparan menggunakan AES-256-GCM.
 *
 * Format penyimpanan di DB: base64( IV[12 bytes] || ciphertext+tag ).
 * IV di-generate acak setiap kali enkripsi, sehingga hasil ciphertext untuk nilai yang
 * sama akan selalu berbeda (itulah kenapa pencarian harus lewat passportNumberHash,
 * bukan lewat kolom ini).
 *
 * Anotasi @Component membuat Spring Boot otomatis meng-inject converter ini via
 * Hibernate's SpringBeanContainer (didukung sejak Spring Boot 2.3+), sehingga
 * encryption key bisa diambil dari application.yml tanpa konfigurasi tambahan.
 */
@Converter
@Component
public class PassportEncryptionConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SecretKeySpec secretKey;

    public PassportEncryptionConverter(@Value("${app.security.encryption-key}") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalStateException(
                    "app.security.encryption-key harus berupa base64 dari 16/24/32 byte (AES-128/192/256). " +
                    "Generate dengan: openssl rand -base64 32");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Gagal mengenkripsi data paspor", e);
        }
    }

    @Override
    public String convertToAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(dbValue);

            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            byte[] cipherText = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] plainBytes = cipher.doFinal(cipherText);

            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Gagal mendekripsi data paspor", e);
        }
    }
}
