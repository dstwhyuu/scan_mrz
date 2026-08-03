package com.hotelfo.scanner.dto.request;

import com.hotelfo.scanner.entity.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Dipakai untuk 2 skenario:
 * 1. Input manual murni (scanLogId = null) — mis. paspor rusak/tidak bisa discan sama sekali.
 * 2. Koreksi hasil scan low-confidence (scanLogId diisi) — agar scan_log terkait ter-link
 *    ke Guest yang akhirnya dibuat, dan statusnya diupdate jadi MANUAL_CORRECTION.
 */
@Getter
@Setter
public class GuestManualEntryRequest {

    private Long scanLogId;

    @NotBlank(message = "Nomor paspor wajib diisi")
    @Size(max = 20, message = "Nomor paspor maksimal 20 karakter")
    private String passportNumber;

    @NotBlank(message = "Negara penerbit wajib diisi")
    @Size(min = 3, max = 3, message = "Kode negara harus 3 huruf (ISO 3166-1 alpha-3), contoh: IDN")
    private String issuingCountry;

    @NotBlank(message = "Nama belakang wajib diisi")
    private String surname;

    @NotBlank(message = "Nama depan wajib diisi")
    private String givenNames;

    @NotBlank(message = "Kebangsaan wajib diisi")
    @Size(min = 3, max = 3, message = "Kode kebangsaan harus 3 huruf (ISO 3166-1 alpha-3), contoh: IDN")
    private String nationality;

    @NotNull(message = "Tanggal lahir wajib diisi")
    @Past(message = "Tanggal lahir harus berada di masa lalu")
    private LocalDate dateOfBirth;

    @NotNull(message = "Jenis kelamin wajib diisi")
    private Gender gender;

    @NotNull(message = "Tanggal kedaluwarsa paspor wajib diisi")
    private LocalDate expiryDate;
}
