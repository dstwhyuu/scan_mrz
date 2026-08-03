package com.hotelfo.scanner.service;

import java.time.LocalDate;

public interface ExcelExportService {
    
    /**
     * Menghasilkan file Excel (.xlsx) berupa byte array untuk laporan tamu
     * yang check-in pada rentang tanggal tertentu.
     * 
     * @param startDate tanggal awal
     * @param endDate tanggal akhir (inklusif)
     * @return byte array dari file Excel
     */
    byte[] generateDailyReport(LocalDate startDate, LocalDate endDate);
}
