package com.hotelfo.scanner.dto.external;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Response mentah dari FastAPI OCR service. Dipetakan otomatis oleh Jackson
 * dari body JSON hasil panggilan RestClient di {@link com.hotelfo.scanner.service.OcrClientService}.
 */
@Getter
@Setter
public class OcrExtractionResponse {
    private boolean success;
    private Double confidence;
    private String mrzLine1;
    private String mrzLine2;
    private boolean checkDigitsValid;
    private OcrFieldsPayload fields;
    private List<String> errors;
}
