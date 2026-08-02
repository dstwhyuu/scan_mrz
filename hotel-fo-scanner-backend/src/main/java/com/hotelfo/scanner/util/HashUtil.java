package com.hotelfo.scanner.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Utility untuk membuat hash deterministik (SHA-256) dari passportNumber,
 * dipakai sebagai lookup key karena passportNumber asli disimpan terenkripsi.
 *
 * Input dinormalisasi (trim + uppercase) agar pencarian tidak sensitif terhadap
 * spasi/kapitalisasi yang mungkin berbeda antar hasil scan OCR.
 */
public final class HashUtil {

    private HashUtil() {
    }

    public static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String normalized = input.trim().toUpperCase();
            byte[] hash = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritma SHA-256 tidak tersedia di JVM ini", e);
        }
    }
}
