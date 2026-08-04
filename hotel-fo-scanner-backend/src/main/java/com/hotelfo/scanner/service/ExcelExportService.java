package com.hotelfo.scanner.service;

import java.time.LocalDate;

public interface ExcelExportService {
    
    /**
     * Menghasilkan file Excel (.xlsx) berupa byte array untuk laporan tamu
     * yang di-scan pada tanggal tertentu.
     * 
     * @param date tanggal scan (berdasarkan created_at di tabel guests)
     * @return byte array dari file Excel
     */
    byte[] generateDailyReport(LocalDate date);
}
