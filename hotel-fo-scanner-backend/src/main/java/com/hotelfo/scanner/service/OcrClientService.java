package com.hotelfo.scanner.service;

import com.hotelfo.scanner.dto.external.OcrExtractionResponse;

public interface OcrClientService {

    /**
     * Mengirim gambar paspor ke FastAPI OCR service untuk diekstrak data MRZ-nya.
     *
     * @throws com.hotelfo.scanner.exception.OcrServiceUnavailableException jika service tidak bisa dihubungi
     */
    OcrExtractionResponse extractMrz(byte[] imageBytes, String filename, String contentType);
}
