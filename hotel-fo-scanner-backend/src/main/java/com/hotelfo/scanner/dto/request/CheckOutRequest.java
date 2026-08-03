package com.hotelfo.scanner.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CheckOutRequest {
    private LocalDate checkOutDate; // opsional - default ke hari ini jika kosong/body tidak dikirim
}
