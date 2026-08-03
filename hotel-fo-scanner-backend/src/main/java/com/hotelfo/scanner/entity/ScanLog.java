package com.hotelfo.scanner.entity;

import com.hotelfo.scanner.entity.enums.ScanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Audit trail setiap aktivitas scan (berhasil maupun gagal). Mapping ke tabel `scan_logs`.
 * Data ini yang nantinya bisa diminta pihak kepolisian/manajemen untuk investigasi.
 */
@Entity
@Table(name = "scan_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", foreignKey = @ForeignKey(name = "fk_scanlogs_guest"))
    private Guest guest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_scanlogs_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('SUCCESS','FAILED','MANUAL_CORRECTION','LOW_CONFIDENCE')")
    private ScanStatus status;

    @Column(name = "ocr_confidence_score", precision = 5, scale = 2)
    private BigDecimal ocrConfidenceScore;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "scanned_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime scannedAt;

    @PrePersist
    protected void onCreate() {
        this.scannedAt = LocalDateTime.now();
    }
}
