package com.hotelfo.scanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Data hasil OCR yang TIDAK memenuhi ambang confidence/checksum untuk disimpan otomatis.
 * Dikirim ke React sebagai draft agar resepsionis tinggal melengkapi/mengoreksi lewat
 * form manual, alih-alih mengetik ulang dari nol.
 */
@Getter
@Builder
@AllArgsConstructor
public class PartialGuestDataResponse {
    private String passportNumber;
    private String issuingCountry;
    private String surname;
    private String givenNames;
    private String nationality;
    private LocalDate dateOfBirth;
    private String gender;
    private LocalDate expiryDate;
}
