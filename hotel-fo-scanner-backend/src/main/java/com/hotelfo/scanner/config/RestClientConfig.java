package com.hotelfo.scanner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * RestClient khusus untuk komunikasi service-to-service ke FastAPI OCR service.
 * Terpisah dari RestClient lain (jika ada di masa depan) agar konfigurasi timeout
 * dan header API key tidak tercampur dengan client HTTP untuk kebutuhan lain.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient ocrRestClient(
            @Value("${app.ocr-service.base-url}") String baseUrl,
            @Value("${app.ocr-service.internal-api-key}") String internalApiKey,
            @Value("${app.ocr-service.timeout-ms}") int timeoutMs) {

        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(timeoutMs))
                .withReadTimeout(Duration.ofMillis(timeoutMs));

        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                // Header wajib agar FastAPI menolak request yang tidak berasal dari Spring Boot ini.
                // Lihat: app/core/security.py di sisi FastAPI (dibangun pada tahap OCR service).
                .defaultHeader("X-Internal-Api-Key", internalApiKey)
                .build();
    }
}
