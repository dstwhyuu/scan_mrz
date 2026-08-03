package com.hotelfo.scanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanLogResponse {
    private Long id;
    private Long guestId;
    private String scannedByUsername;
    private String status;
    private BigDecimal ocrConfidenceScore;
    private String errorMessage;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime scannedAt;
}
