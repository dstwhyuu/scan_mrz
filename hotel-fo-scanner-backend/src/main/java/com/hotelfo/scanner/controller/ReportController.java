package com.hotelfo.scanner.controller;

import com.hotelfo.scanner.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ExcelExportService excelExportService;

    /**
     * Endpoint untuk mendownload laporan harian tamu dalam bentuk file Excel (.xlsx).
     * Dapat diakses oleh FRONT_OFFICE, ADMIN, dan SUPERVISOR.
     */
    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('FRONT_OFFICE', 'ADMIN', 'SUPERVISOR')")
    public ResponseEntity<Resource> downloadDailyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        // Jika date kosong, default ke hari ini
        LocalDate filterDate = (date != null) ? date : LocalDate.now();

        byte[] excelData = excelExportService.generateDailyReport(filterDate, filterDate);
        ByteArrayResource resource = new ByteArrayResource(excelData);

        String filename = "Laporan_Tamu_" + filterDate.toString() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excelData.length)
                .body(resource);
    }
}
