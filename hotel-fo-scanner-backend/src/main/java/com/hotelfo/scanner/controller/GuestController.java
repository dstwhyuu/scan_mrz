package com.hotelfo.scanner.controller;

import com.hotelfo.scanner.dto.request.GuestManualEntryRequest;
import com.hotelfo.scanner.dto.response.GuestResponse;
import com.hotelfo.scanner.entity.Guest;
import com.hotelfo.scanner.mapper.GuestMapper;
import com.hotelfo.scanner.service.GuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    /**
     * Dipakai untuk 2 skenario: input manual murni, ATAU koreksi hasil scan
     * low-confidence (sertakan scanLogId di body agar scan_log terkait ter-link).
     */
    @PostMapping
    public ResponseEntity<GuestResponse> createManualEntry(@Valid @RequestBody GuestManualEntryRequest request) {
        Guest guest = guestService.createFromManualEntry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(GuestMapper.toResponse(guest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuestResponse> getById(@PathVariable Long id) {
        Guest guest = guestService.getById(id);
        return ResponseEntity.ok(GuestMapper.toResponse(guest));
    }
}
