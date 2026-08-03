package com.hotelfo.scanner.exception;

/**
 * Dilempar saat FastAPI OCR service tidak bisa dihubungi (down, timeout, network error).
 * Sengaja dipisah dari exception generik agar bisa dipetakan ke HTTP 503, bukan 500 —
 * membedakan "server kami rusak" dari "service lain sedang bermasalah".
 */
public class OcrServiceUnavailableException extends RuntimeException {
    public OcrServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
