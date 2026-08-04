package com.hotelfo.scanner.controller;

import com.hotelfo.scanner.dto.response.ScanLogResponse;
import com.hotelfo.scanner.entity.enums.ScanStatus;
import com.hotelfo.scanner.service.ScanLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/scan-logs")
@RequiredArgsConstructor
public class ScanLogController {

    private final ScanLogService scanLogService;

    /**
     * Endpoint untuk melihat audit trail scan log.
     * Bisa diakses oleh semua user yang sudah login.
     */
    @GetMapping
    public ResponseEntity<Page<ScanLogResponse>> getScanLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) ScanStatus status,
            @PageableDefault(sort = "scannedAt", direction = Sort.Direction.DESC, size = 20) Pageable pageable) {
        
        Page<ScanLogResponse> response = scanLogService.getScanLogs(date, status, pageable);
        return ResponseEntity.ok(response);
    }
}
