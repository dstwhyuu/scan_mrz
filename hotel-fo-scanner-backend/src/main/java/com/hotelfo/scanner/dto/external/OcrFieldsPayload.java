package com.hotelfo.scanner.dto.external;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Representasi field MRZ yang sudah diparsing oleh FastAPI OCR service.
 * Struktur ini HARUS sinkron dengan skema JSON yang dikembalikan endpoint
 * POST /internal/v1/ocr/extract-mrz milik service Python.
 */
@Getter
@Setter
public class OcrFieldsPayload {
    private String documentType;
    private String issuingCountry;
    private String surname;
    private String givenNames;
    private String passportNumber;
    private String nationality;
    private LocalDate dateOfBirth;
    private String gender;      // "M" / "F" / "X" — dikonversi ke enum Gender saat disimpan
    private LocalDate expiryDate;
}
