package com.hotelfo.scanner.controller;

import com.hotelfo.scanner.dto.request.AssignRoomRequest;
import com.hotelfo.scanner.dto.request.CheckOutRequest;
import com.hotelfo.scanner.dto.response.GuestVisitResponse;
import com.hotelfo.scanner.service.GuestVisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Endpoint yang scope-nya per record visit (bukan per guest): assign kamar & check-out. */
@RestController
@RequestMapping("/api/v1/visits/{visitId}")
@RequiredArgsConstructor
public class VisitController {

    private final GuestVisitService guestVisitService;

    @PatchMapping("/room")
    public ResponseEntity<GuestVisitResponse> assignRoom(
            @PathVariable Long visitId,
            @Valid @RequestBody AssignRoomRequest request) {

        return ResponseEntity.ok(guestVisitService.assignRoom(visitId, request.getRoomNumber()));
    }

    @PatchMapping("/check-out")
    public ResponseEntity<GuestVisitResponse> checkOut(
            @PathVariable Long visitId,
            @RequestBody(required = false) CheckOutRequest request) {

        var checkOutDate = request != null ? request.getCheckOutDate() : null;
        return ResponseEntity.ok(guestVisitService.checkOut(visitId, checkOutDate));
    }
}
