package com.hotelfo.scanner.mapper;

import com.hotelfo.scanner.dto.response.GuestVisitResponse;
import com.hotelfo.scanner.entity.GuestVisit;

/**
 * PENTING: hanya dipanggil dari dalam method @Transactional di service layer,
 * TIDAK dari controller. GuestVisit.guest dan GuestVisit.createdBy adalah relasi
 * lazy (FetchType.LAZY) — karena app.jpa.open-in-view = false, mengaksesnya di luar
 * transaksi (mis. di controller) akan melempar LazyInitializationException.
 */
public final class GuestVisitMapper {

    private GuestVisitMapper() {
    }

    public static GuestVisitResponse toResponse(GuestVisit visit) {
        return GuestVisitResponse.builder()
                .id(visit.getId())
                .guest(GuestMapper.toResponse(visit.getGuest()))
                .roomNumber(visit.getRoomNumber())
                .checkInDate(visit.getCheckInDate())
                .checkOutDate(visit.getCheckOutDate())
                .purposeOfStay(visit.getPurposeOfStay())
                .createdByUsername(visit.getCreatedBy().getUsername())
                .createdAt(visit.getCreatedAt())
                .build();
    }
}
