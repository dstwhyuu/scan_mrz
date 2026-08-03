package com.hotelfo.scanner.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CheckInRequest {

    @Size(max = 10, message = "Nomor kamar maksimal 10 karakter")
    private String roomNumber; // opsional - bisa diisi belakangan lewat endpoint assign-room

    private LocalDate checkInDate; // opsional - default ke hari ini jika kosong

    @Size(max = 100, message = "Tujuan kunjungan maksimal 100 karakter")
    private String purposeOfStay;
}
