package com.hotelfo.scanner.service.impl;

import com.hotelfo.scanner.dto.request.CheckInRequest;
import com.hotelfo.scanner.dto.response.GuestVisitResponse;
import com.hotelfo.scanner.entity.Guest;
import com.hotelfo.scanner.entity.GuestVisit;
import com.hotelfo.scanner.entity.User;
import com.hotelfo.scanner.exception.ResourceNotFoundException;
import com.hotelfo.scanner.mapper.GuestVisitMapper;
import com.hotelfo.scanner.repository.GuestRepository;
import com.hotelfo.scanner.repository.GuestVisitRepository;
import com.hotelfo.scanner.repository.UserRepository;
import com.hotelfo.scanner.security.CustomUserPrincipal;
import com.hotelfo.scanner.service.GuestVisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestVisitServiceImpl implements GuestVisitService {

    private final GuestVisitRepository guestVisitRepository;
    private final GuestRepository guestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public GuestVisitResponse checkIn(Long guestId, CheckInRequest request, CustomUserPrincipal currentUser) {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest dengan id " + guestId + " tidak ditemukan"));

        guestVisitRepository.findByGuestIdAndCheckOutDateIsNull(guestId).ifPresent(active -> {
            throw new IllegalStateException(
                    "Tamu ini masih berstatus check-in aktif (visit id " + active.getId()
                            + "). Check-out dulu sebelum membuat check-in baru.");
        });

        // findById dipakai (bukan getReferenceById) supaya entity User sudah terinisialisasi
        // penuh saat dimapping ke DTO nanti, menghindari LazyInitializationException.
        User createdBy = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        GuestVisit visit = GuestVisit.builder()
                .guest(guest)
                .roomNumber(request.getRoomNumber())
                .checkInDate(request.getCheckInDate() != null ? request.getCheckInDate() : LocalDate.now())
                .purposeOfStay(request.getPurposeOfStay())
                .createdBy(createdBy)
                .build();

        GuestVisit saved = guestVisitRepository.save(visit);
        return GuestVisitMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public GuestVisitResponse assignRoom(Long visitId, String roomNumber) {
        GuestVisit visit = getVisitOrThrow(visitId);

        if (visit.getCheckOutDate() != null) {
            throw new IllegalStateException("Tidak bisa mengubah kamar, tamu ini sudah check-out");
        }

        visit.setRoomNumber(roomNumber);
        GuestVisit saved = guestVisitRepository.save(visit);
        return GuestVisitMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public GuestVisitResponse checkOut(Long visitId, LocalDate checkOutDate) {
        GuestVisit visit = getVisitOrThrow(visitId);

        if (visit.getCheckOutDate() != null) {
            throw new IllegalStateException("Tamu ini sudah check-out sebelumnya pada " + visit.getCheckOutDate());
        }

        LocalDate resolvedDate = checkOutDate != null ? checkOutDate : LocalDate.now();
        if (resolvedDate.isBefore(visit.getCheckInDate())) {
            throw new IllegalArgumentException("Tanggal check-out tidak boleh sebelum tanggal check-in ("
                    + visit.getCheckInDate() + ")");
        }

        visit.setCheckOutDate(resolvedDate);
        GuestVisit saved = guestVisitRepository.save(visit);
        return GuestVisitMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuestVisitResponse> getVisitHistory(Long guestId) {
        if (!guestRepository.existsById(guestId)) {
            throw new ResourceNotFoundException("Guest dengan id " + guestId + " tidak ditemukan");
        }
        return guestVisitRepository.findByGuestId(guestId).stream()
                .map(GuestVisitMapper::toResponse)
                .toList();
    }

    private GuestVisit getVisitOrThrow(Long visitId) {
        return guestVisitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit dengan id " + visitId + " tidak ditemukan"));
    }
}
