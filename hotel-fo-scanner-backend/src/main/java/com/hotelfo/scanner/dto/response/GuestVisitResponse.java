package com.hotelfo.scanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class GuestVisitResponse {
    private Long id;
    private GuestResponse guest;
    private String roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String purposeOfStay;
    private String createdByUsername;
    private LocalDateTime createdAt;
}
