package com.hotelfo.scanner.service.impl;

import com.hotelfo.scanner.dto.external.OcrExtractionResponse;
import com.hotelfo.scanner.exception.OcrServiceUnavailableException;
import com.hotelfo.scanner.service.OcrClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrClientServiceImpl implements OcrClientService {

    private static final String EXTRACT_MRZ_PATH = "/internal/v1/ocr/extract-mrz";

    private final RestClient ocrRestClient;

    @Override
    public OcrExtractionResponse extractMrz(byte[] imageBytes, String filename, String contentType) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", buildImageResource(imageBytes, filename));

        try {
            return ocrRestClient.post()
                    .uri(EXTRACT_MRZ_PATH)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(OcrExtractionResponse.class);
        } catch (RestClientException e) {
            log.error("Gagal memanggil OCR service di {}: {}", EXTRACT_MRZ_PATH, e.getMessage());
            throw new OcrServiceUnavailableException("Tidak dapat menghubungi layanan OCR", e);
        }
    }

    private ByteArrayResource buildImageResource(byte[] imageBytes, String filename) {
        // Override getFilename() supaya multipart request punya nama file yang valid,
        // ByteArrayResource polos tidak punya nama file secara default.
        return new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return filename != null ? filename : "passport-scan.jpg";
            }
        };
    }
}
