package com.hotelfo.scanner.repository;

import com.hotelfo.scanner.entity.ScanLog;
import com.hotelfo.scanner.entity.enums.ScanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ScanLogRepository extends JpaRepository<ScanLog, Long> {

    Page<ScanLog> findByStatusAndScannedAtBetween(
            ScanStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<ScanLog> findByScannedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
