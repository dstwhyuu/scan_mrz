package com.hotelfo.scanner.service;

import com.hotelfo.scanner.dto.external.OcrFieldsPayload;
import com.hotelfo.scanner.dto.request.GuestManualEntryRequest;
import com.hotelfo.scanner.entity.Guest;

public interface GuestService {

    /**
     * Cari guest existing berdasarkan hash nomor paspor + negara penerbit.
     * Jika belum ada, buat baru. Jika sudah ada (tamu repeat guest), field yang mungkin
     * berubah (nama, expiry, dsb — kasus ganti paspor/re-entry) akan diperbarui.
     */
    Guest findOrCreateFromOcr(OcrFieldsPayload fields, String mrzLine1, String mrzLine2, boolean checkDigitsValid);

    /**
     * Membuat/memperbarui Guest dari input manual resepsionis (fallback saat OCR gagal/low-confidence).
     * Jika request.scanLogId diisi, scan_log terkait akan di-link ke Guest ini dan
     * statusnya diubah menjadi MANUAL_CORRECTION.
     */
    Guest createFromManualEntry(GuestManualEntryRequest request);

    Guest getById(Long id);
}
