package com.hotelfo.scanner.service;

import com.hotelfo.scanner.dto.request.CheckInRequest;
import com.hotelfo.scanner.dto.response.GuestVisitResponse;
import com.hotelfo.scanner.security.CustomUserPrincipal;

import java.time.LocalDate;
import java.util.List;

public interface GuestVisitService {

    /** Menolak jika tamu masih punya visit aktif (belum check-out) — 1 tamu 1 visit aktif. */
    GuestVisitResponse checkIn(Long guestId, CheckInRequest request, CustomUserPrincipal currentUser);

    GuestVisitResponse assignRoom(Long visitId, String roomNumber);

    GuestVisitResponse checkOut(Long visitId, LocalDate checkOutDate);

    List<GuestVisitResponse> getVisitHistory(Long guestId);
}
