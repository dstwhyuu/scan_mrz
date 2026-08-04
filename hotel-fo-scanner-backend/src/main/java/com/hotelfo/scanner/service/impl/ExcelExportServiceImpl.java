package com.hotelfo.scanner.service.impl;

import com.hotelfo.scanner.entity.Guest;
import com.hotelfo.scanner.repository.GuestRepository;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelExportServiceImpl implements ExcelExportService {

    private final GuestRepository guestRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] generateDailyReport(LocalDate date) {
        log.info("Generating daily report for date {}", date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Guest> guests = guestRepository.findByCreatedAtBetween(startOfDay, endOfDay);

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
                "Tgl Expire", "Tanggal Scan"
            };

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            // Create Data Rows
            int rowIdx = 1;
            for (Guest guest : guests) {
                Row row = sheet.createRow(rowIdx);

                createCell(row, 0, String.valueOf(rowIdx), rowCellStyle);
                createCell(row, 1, guest.getPassportNumber(), rowCellStyle);
                createCell(row, 2, guest.getIssuingCountry(), rowCellStyle);
                createCell(row, 3, guest.getNationality(), rowCellStyle);
                createCell(row, 4, guest.getGivenNames(), rowCellStyle);
                createCell(row, 5, guest.getSurname(), rowCellStyle);
                createCell(row, 6, guest.getDateOfBirth() != null ? guest.getDateOfBirth().format(dateFormatter) : "-", rowCellStyle);
                createCell(row, 7, guest.getGender() != null ? guest.getGender().name() : "-", rowCellStyle);
                createCell(row, 8, guest.getExpiryDate() != null ? guest.getExpiryDate().format(dateFormatter) : "-", rowCellStyle);
                createCell(row, 9, guest.getCreatedAt() != null ? guest.getCreatedAt().format(dateTimeFormatter) : "-", rowCellStyle);

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
