package com.hotelfo.scanner.service;

import com.hotelfo.scanner.dto.response.ScanLogResponse;
import com.hotelfo.scanner.entity.enums.ScanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ScanLogService {
    
    /**
     * Mendapatkan daftar audit trail scan log.
     * Dapat difilter berdasarkan tanggal dan status.
     */
    Page<ScanLogResponse> getScanLogs(LocalDate date, ScanStatus status, Pageable pageable);
}
