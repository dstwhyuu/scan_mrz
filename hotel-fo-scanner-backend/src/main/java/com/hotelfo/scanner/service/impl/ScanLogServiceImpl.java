package com.hotelfo.scanner.service.impl;

import com.hotelfo.scanner.dto.response.ScanLogResponse;
import com.hotelfo.scanner.entity.ScanLog;
import com.hotelfo.scanner.entity.enums.ScanStatus;
import com.hotelfo.scanner.mapper.ScanLogMapper;
import com.hotelfo.scanner.repository.ScanLogRepository;
import com.hotelfo.scanner.service.ScanLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ScanLogServiceImpl implements ScanLogService {

    private final ScanLogRepository scanLogRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ScanLogResponse> getScanLogs(LocalDate date, ScanStatus status, Pageable pageable) {
        
        LocalDateTime startOfDay;
        LocalDateTime endOfDay;
        
        if (date != null) {
            startOfDay = date.atStartOfDay();
            endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1);
        } else {
            // Default: 30 hari terakhir jika tidak ada tanggal yang diset
            startOfDay = LocalDate.now().minusDays(30).atStartOfDay();
            endOfDay = LocalDate.now().plusDays(1).atStartOfDay().minusNanos(1);
        }

        Page<ScanLog> scanLogsPage;

        if (status != null) {
            scanLogsPage = scanLogRepository.findByStatusAndScannedAtBetween(status, startOfDay, endOfDay, pageable);
        } else {
            scanLogsPage = scanLogRepository.findByScannedAtBetween(startOfDay, endOfDay, pageable);
        }

        return scanLogsPage.map(ScanLogMapper::toResponse);
    }
}
