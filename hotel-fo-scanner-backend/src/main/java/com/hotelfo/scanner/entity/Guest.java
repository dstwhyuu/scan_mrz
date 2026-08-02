package com.hotelfo.scanner.entity;

import com.hotelfo.scanner.entity.enums.Gender;
import com.hotelfo.scanner.util.PassportEncryptionConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Master data identitas tamu, hasil ekstraksi MRZ. Mapping ke tabel `guests`.
 *
 * PENTING: `passportNumber` dienkripsi secara transparan oleh {@link PassportEncryptionConverter}
 * sebelum disimpan ke kolom `passport_number`. Untuk pencarian/uniqueness, gunakan
 * `passportNumberHash` (SHA-256), BUKAN query langsung ke kolom terenkripsi.
 */
@Entity
@Table(name = "guests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_type", nullable = false, length = 1)
    @Builder.Default
    private String documentType = "P";

    @Convert(converter = PassportEncryptionConverter.class)
    @Column(name = "passport_number", nullable = false, length = 255)
    private String passportNumber;

    @Column(name = "passport_number_hash", nullable = false, length = 64)
    private String passportNumberHash;

    @Column(name = "issuing_country", nullable = false, length = 3)
    private String issuingCountry;

    @Column(nullable = false, length = 100)
    private String surname;

    @Column(name = "given_names", nullable = false, length = 100)
    private String givenNames;

    @Column(nullable = false, length = 3)
    private String nationality;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Gender gender;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "mrz_line1", length = 44)
    private String mrzLine1;

    @Column(name = "mrz_line2", length = 44)
    private String mrzLine2;

    @Column(name = "check_digits_valid", nullable = false)
    @Builder.Default
    private boolean checkDigitsValid = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
