package com.hotelfo.scanner.mapper;

import com.hotelfo.scanner.dto.external.OcrFieldsPayload;
import com.hotelfo.scanner.dto.response.GuestResponse;
import com.hotelfo.scanner.dto.response.PartialGuestDataResponse;
import com.hotelfo.scanner.entity.Guest;

/**
 * Mapper manual (plain Java, bukan MapStruct) agar tidak menambah kompleksitas
 * annotation-processor chain untuk boilerplate awal. Bisa dimigrasikan ke MapStruct
 * kapan pun kebutuhan mapping makin kompleks.
 */
public final class GuestMapper {

    private GuestMapper() {
    }

    public static GuestResponse toResponse(Guest guest) {
        return GuestResponse.builder()
                .id(guest.getId())
                .passportNumber(guest.getPassportNumber())
                .issuingCountry(guest.getIssuingCountry())
                .surname(guest.getSurname())
                .givenNames(guest.getGivenNames())
                .nationality(guest.getNationality())
                .dateOfBirth(guest.getDateOfBirth())
                .gender(guest.getGender() != null ? guest.getGender().name() : null)
                .expiryDate(guest.getExpiryDate())
                .checkDigitsValid(guest.isCheckDigitsValid())
                .build();
    }

    public static PartialGuestDataResponse toPartialResponse(OcrFieldsPayload fields) {
        if (fields == null) {
            return null;
        }
        return PartialGuestDataResponse.builder()
                .passportNumber(fields.getPassportNumber())
                .issuingCountry(fields.getIssuingCountry())
                .surname(fields.getSurname())
                .givenNames(fields.getGivenNames())
                .nationality(fields.getNationality())
                .dateOfBirth(fields.getDateOfBirth())
                .gender(fields.getGender())
                .expiryDate(fields.getExpiryDate())
                .build();
    }
}
