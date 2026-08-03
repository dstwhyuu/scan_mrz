package com.hotelfo.scanner.service.impl;

import com.hotelfo.scanner.entity.Guest;
import com.hotelfo.scanner.entity.GuestVisit;
import com.hotelfo.scanner.repository.GuestVisitRepository;
import com.hotelfo.scanner.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelExportServiceImpl implements ExcelExportService {

    private final GuestVisitRepository guestVisitRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] generateDailyReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating daily report for dates {} to {}", startDate, endDate);
        
        List<GuestVisit> visits = guestVisitRepository.findByCheckInDateBetween(startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Laporan Tamu");

            // Header Font
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLACK.getIndex());

            // Header Style
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBorderTop(BorderStyle.THIN);
            headerCellStyle.setBorderLeft(BorderStyle.THIN);
            headerCellStyle.setBorderRight(BorderStyle.THIN);

            // Row Style
            CellStyle rowCellStyle = workbook.createCellStyle();
            rowCellStyle.setBorderBottom(BorderStyle.THIN);
            rowCellStyle.setBorderTop(BorderStyle.THIN);
            rowCellStyle.setBorderLeft(BorderStyle.THIN);
            rowCellStyle.setBorderRight(BorderStyle.THIN);

            // Columns
            String[] columns = {
                "No", "No. Paspor", "Negara Penerbit", "Kewarganegaraan", 
                "Nama Depan", "Nama Belakang", "Tgl Lahir", "Jenis Kelamin",
                "Tgl Check-in", "Tgl Check-out", "Kamar", "Tujuan Menginap", "Resepsionis"
            };

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Create Data Rows
            int rowIdx = 1;
            for (GuestVisit visit : visits) {
                Row row = sheet.createRow(rowIdx);
                Guest guest = visit.getGuest();

                createCell(row, 0, String.valueOf(rowIdx), rowCellStyle);
                createCell(row, 1, guest.getPassportNumber(), rowCellStyle);
                createCell(row, 2, guest.getIssuingCountry(), rowCellStyle);
                createCell(row, 3, guest.getNationality(), rowCellStyle);
                createCell(row, 4, guest.getGivenNames(), rowCellStyle);
                createCell(row, 5, guest.getSurname(), rowCellStyle);
                createCell(row, 6, guest.getDateOfBirth() != null ? guest.getDateOfBirth().format(dateFormatter) : "-", rowCellStyle);
                createCell(row, 7, guest.getGender() != null ? guest.getGender().name() : "-", rowCellStyle);
                createCell(row, 8, visit.getCheckInDate() != null ? visit.getCheckInDate().format(dateFormatter) : "-", rowCellStyle);
                createCell(row, 9, visit.getCheckOutDate() != null ? visit.getCheckOutDate().format(dateFormatter) : "-", rowCellStyle);
                createCell(row, 10, visit.getRoomNumber() != null ? visit.getRoomNumber() : "-", rowCellStyle);
                createCell(row, 11, visit.getPurposeOfStay() != null ? visit.getPurposeOfStay() : "-", rowCellStyle);
                createCell(row, 12, visit.getCreatedBy() != null ? visit.getCreatedBy().getUsername() : "-", rowCellStyle);

                rowIdx++;
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate Excel file", e);
            throw new RuntimeException("Gagal membuat file laporan Excel", e);
        }
    }

    private void createCell(Row row, int columnCount, String value, CellStyle style) {
        Cell cell = row.createCell(columnCount);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
