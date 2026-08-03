package com.hotelfo.scanner.repository;

import com.hotelfo.scanner.entity.GuestVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GuestVisitRepository extends JpaRepository<GuestVisit, Long> {

    // Dipakai oleh ReportService untuk generate laporan harian
    List<GuestVisit> findByCheckInDateBetween(LocalDate start, LocalDate end);

    List<GuestVisit> findByGuestId(Long guestId);

    // Dipakai untuk mencegah 1 tamu punya 2 visit aktif (belum check-out) di waktu bersamaan
    Optional<GuestVisit> findByGuestIdAndCheckOutDateIsNull(Long guestId);
}
