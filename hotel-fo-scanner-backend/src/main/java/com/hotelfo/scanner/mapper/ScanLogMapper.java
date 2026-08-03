package com.hotelfo.scanner.mapper;

import com.hotelfo.scanner.dto.response.ScanLogResponse;
import com.hotelfo.scanner.entity.ScanLog;

public final class ScanLogMapper {

    private ScanLogMapper() {
    }

    public static ScanLogResponse toResponse(ScanLog scanLog) {
        if (scanLog == null) {
            return null;
        }

        return ScanLogResponse.builder()
                .id(scanLog.getId())
                .guestId(scanLog.getGuest() != null ? scanLog.getGuest().getId() : null)
                .scannedByUsername(scanLog.getUser() != null ? scanLog.getUser().getUsername() : null)
                .status(scanLog.getStatus() != null ? scanLog.getStatus().name() : null)
                .ocrConfidenceScore(scanLog.getOcrConfidenceScore())
                .errorMessage(scanLog.getErrorMessage())
                .ipAddress(scanLog.getIpAddress())
                .userAgent(scanLog.getUserAgent())
                .scannedAt(scanLog.getScannedAt())
                .build();
    }
}
