package com.hotelfo.scanner.repository;

import com.hotelfo.scanner.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    // Pencarian dilakukan lewat hash karena passportNumber tersimpan terenkripsi di DB.
    Optional<Guest> findByPassportNumberHashAndIssuingCountry(String passportNumberHash, String issuingCountry);

    // Untuk laporan harian: ambil semua tamu yang di-scan pada rentang waktu tertentu.
    List<Guest> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
