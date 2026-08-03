package com.hotelfo.scanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ScanResultResponse {
    private Long scanLogId;
    private String status;
    private Double confidenceScore;
    private GuestResponse guest;
    private PartialGuestDataResponse partialData;
    private String message;
    private boolean requiresManualReview;
}
