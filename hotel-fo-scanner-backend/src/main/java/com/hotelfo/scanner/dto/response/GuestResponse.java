package com.hotelfo.scanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class GuestResponse {
    private Long id;
    private String passportNumber;
    private String issuingCountry;
    private String surname;
    private String givenNames;
    private String nationality;
    private LocalDate dateOfBirth;
    private String gender;
    private LocalDate expiryDate;
    private boolean checkDigitsValid;
}
