package com.hotelfo.scanner.controller;

import com.hotelfo.scanner.dto.response.ScanResultResponse;
import com.hotelfo.scanner.security.CustomUserPrincipal;
import com.hotelfo.scanner.service.ScanOrchestratorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/scans")
@RequiredArgsConstructor
public class ScanController {

    private final ScanOrchestratorService scanOrchestratorService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ScanResultResponse> uploadScan(
            @RequestParam("passportImage") MultipartFile passportImage,
            @AuthenticationPrincipal CustomUserPrincipal currentUser,
            HttpServletRequest request) {

        ScanResultResponse result = scanOrchestratorService.processScan(passportImage, currentUser, request);

        // 200 untuk hasil yang berhasil disimpan, 422 saat React perlu menampilkan form koreksi manual.
        HttpStatus status = result.isRequiresManualReview() ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.OK;
        return ResponseEntity.status(status).body(result);
    }
}
