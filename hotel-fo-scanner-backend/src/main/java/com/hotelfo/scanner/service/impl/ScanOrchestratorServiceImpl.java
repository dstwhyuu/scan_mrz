package com.hotelfo.scanner.service.impl;

import com.hotelfo.scanner.dto.external.OcrExtractionResponse;
import com.hotelfo.scanner.dto.response.ScanResultResponse;
import com.hotelfo.scanner.entity.Guest;
import com.hotelfo.scanner.entity.ScanLog;
import com.hotelfo.scanner.entity.User;
import com.hotelfo.scanner.entity.enums.ScanStatus;
import com.hotelfo.scanner.exception.OcrServiceUnavailableException;
import com.hotelfo.scanner.mapper.GuestMapper;
import com.hotelfo.scanner.repository.ScanLogRepository;
import com.hotelfo.scanner.repository.UserRepository;
import com.hotelfo.scanner.security.CustomUserPrincipal;
import com.hotelfo.scanner.service.GuestService;
import com.hotelfo.scanner.service.OcrClientService;
import com.hotelfo.scanner.service.ScanOrchestratorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanOrchestratorServiceImpl implements ScanOrchestratorService {

    // Di bawah ambang ini, hasil OCR dianggap tidak cukup andal untuk disimpan otomatis.
    private static final double CONFIDENCE_THRESHOLD = 75.0;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

    private final OcrClientService ocrClientService;
    private final GuestService guestService;
    private final ScanLogRepository scanLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ScanResultResponse processScan(MultipartFile file, CustomUserPrincipal currentUser, HttpServletRequest request) {
        validateFile(file);

        // getReferenceById: proxy tanpa query tambahan, aman karena user pasti ada (sudah lolos JWT auth)
        User user = userRepository.getReferenceById(currentUser.getId());
        String ipAddress = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        OcrExtractionResponse ocrResult;
        try {
            ocrResult = ocrClientService.extractMrz(readBytes(file), file.getOriginalFilename(), file.getContentType());
        } catch (OcrServiceUnavailableException e) {
            ScanLog failedLog = saveScanLog(null, user, ScanStatus.FAILED, null, e.getMessage(), ipAddress, userAgent);
            return ScanResultResponse.builder()
                    .scanLogId(failedLog.getId())
                    .status(ScanStatus.FAILED.name())
                    .message("Layanan OCR sedang tidak tersedia. Silakan coba lagi atau input data secara manual.")
                    .requiresManualReview(true)
                    .build();
        }

        BigDecimal confidence = ocrResult.getConfidence() != null ? BigDecimal.valueOf(ocrResult.getConfidence()) : null;

        if (!qualifiesForAutoSave(ocrResult)) {
            return handleUnqualifiedScan(ocrResult, user, confidence, ipAddress, userAgent);
        }

        Guest guest = guestService.findOrCreateFromOcr(
                ocrResult.getFields(), ocrResult.getMrzLine1(), ocrResult.getMrzLine2(), true);

        ScanLog savedLog = saveScanLog(guest, user, ScanStatus.SUCCESS, confidence, null, ipAddress, userAgent);

        return ScanResultResponse.builder()
                .scanLogId(savedLog.getId())
                .status(ScanStatus.SUCCESS.name())
                .confidenceScore(ocrResult.getConfidence())
                .guest(GuestMapper.toResponse(guest))
                .requiresManualReview(false)
                .build();
    }

    private boolean qualifiesForAutoSave(OcrExtractionResponse result) {
        return result.isSuccess()
                && result.getFields() != null
                && result.getConfidence() != null
                && result.getConfidence() >= CONFIDENCE_THRESHOLD
                && result.isCheckDigitsValid();
    }

    private ScanResultResponse handleUnqualifiedScan(OcrExtractionResponse ocrResult, User user, BigDecimal confidence,
                                                       String ipAddress, String userAgent) {
        ScanStatus status = ocrResult.isSuccess() ? ScanStatus.LOW_CONFIDENCE : ScanStatus.FAILED;
        String errorMessage = joinErrors(ocrResult.getErrors());

        ScanLog log = saveScanLog(null, user, status, confidence, errorMessage, ipAddress, userAgent);

        return ScanResultResponse.builder()
                .scanLogId(log.getId())
                .status(status.name())
                .confidenceScore(ocrResult.getConfidence())
                .partialData(GuestMapper.toPartialResponse(ocrResult.getFields()))
                .message("Beberapa data MRZ tidak terbaca dengan jelas. Mohon periksa dan lengkapi secara manual.")
                .requiresManualReview(true)
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File gambar tidak boleh kosong");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Ukuran file maksimal 5MB");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Format file harus JPG atau PNG");
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Gagal membaca file gambar yang diupload", e);
        }
    }

    private ScanLog saveScanLog(Guest guest, User user, ScanStatus status, BigDecimal confidence,
                                 String errorMessage, String ipAddress, String userAgent) {
        ScanLog log = ScanLog.builder()
                .guest(guest)
                .user(user)
                .status(status)
                .ocrConfidenceScore(confidence)
                .errorMessage(errorMessage)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        return scanLogRepository.save(log);
    }

    private String joinErrors(List<String> errors) {
        return (errors == null || errors.isEmpty()) ? null : String.join("; ", errors);
    }

    private String extractClientIp(HttpServletRequest request) {
        // Di belakang reverse proxy (Nginx), IP asli client ada di X-Forwarded-For, bukan getRemoteAddr().
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
