package com.hotelfo.scanner.service;

import com.hotelfo.scanner.dto.response.ScanResultResponse;
import com.hotelfo.scanner.security.CustomUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ScanOrchestratorService {

    /**
     * Alur lengkap: validasi file -> panggil OCR service -> evaluasi hasil ->
     * simpan/tidak simpan Guest -> catat scan_log -> kembalikan response ke React.
     */
    ScanResultResponse processScan(MultipartFile file, CustomUserPrincipal currentUser, HttpServletRequest request);
}
