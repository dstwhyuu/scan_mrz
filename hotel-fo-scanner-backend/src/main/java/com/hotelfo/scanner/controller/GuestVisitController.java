package com.hotelfo.scanner.controller;

import com.hotelfo.scanner.dto.request.CheckInRequest;
import com.hotelfo.scanner.dto.response.GuestVisitResponse;
import com.hotelfo.scanner.security.CustomUserPrincipal;
import com.hotelfo.scanner.service.GuestVisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoint yang scope-nya di bawah satu Guest tertentu: check-in baru & riwayat kunjungan. */
@RestController
@RequestMapping("/api/v1/guests/{guestId}/visits")
@RequiredArgsConstructor
public class GuestVisitController {

    private final GuestVisitService guestVisitService;

    @PostMapping
    public ResponseEntity<GuestVisitResponse> checkIn(
            @PathVariable Long guestId,
            @Valid @RequestBody CheckInRequest request,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        GuestVisitResponse response = guestVisitService.checkIn(guestId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GuestVisitResponse>> getHistory(@PathVariable Long guestId) {
        return ResponseEntity.ok(guestVisitService.getVisitHistory(guestId));
    }
}
