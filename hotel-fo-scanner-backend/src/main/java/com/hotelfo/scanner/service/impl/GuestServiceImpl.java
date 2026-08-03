package com.hotelfo.scanner.service.impl;

import com.hotelfo.scanner.dto.external.OcrFieldsPayload;
import com.hotelfo.scanner.dto.request.GuestManualEntryRequest;
import com.hotelfo.scanner.entity.Guest;
import com.hotelfo.scanner.entity.ScanLog;
import com.hotelfo.scanner.entity.enums.Gender;
import com.hotelfo.scanner.entity.enums.ScanStatus;
import com.hotelfo.scanner.exception.ResourceNotFoundException;
import com.hotelfo.scanner.repository.GuestRepository;
import com.hotelfo.scanner.repository.ScanLogRepository;
import com.hotelfo.scanner.service.GuestService;
import com.hotelfo.scanner.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;
    private final ScanLogRepository scanLogRepository;

    @Override
    @Transactional
    public Guest findOrCreateFromOcr(OcrFieldsPayload fields, String mrzLine1, String mrzLine2, boolean checkDigitsValid) {
        String hash = HashUtil.sha256Hex(fields.getPassportNumber());

        return guestRepository.findByPassportNumberHashAndIssuingCountry(hash, fields.getIssuingCountry())
                .map(existing -> updateFromOcr(existing, fields, mrzLine1, mrzLine2, checkDigitsValid))
                .orElseGet(() -> createFromOcr(fields, mrzLine1, mrzLine2, checkDigitsValid, hash));
    }

    private Guest createFromOcr(OcrFieldsPayload fields, String mrzLine1, String mrzLine2,
                                 boolean checkDigitsValid, String hash) {
        Guest guest = Guest.builder()
                .documentType(fields.getDocumentType() != null ? fields.getDocumentType() : "P")
                .passportNumber(fields.getPassportNumber())
                .passportNumberHash(hash)
                .issuingCountry(fields.getIssuingCountry())
                .surname(fields.getSurname())
                .givenNames(fields.getGivenNames())
                .nationality(fields.getNationality())
                .dateOfBirth(fields.getDateOfBirth())
                .gender(Gender.valueOf(fields.getGender()))
                .expiryDate(fields.getExpiryDate())
                .mrzLine1(mrzLine1)
                .mrzLine2(mrzLine2)
                .checkDigitsValid(checkDigitsValid)
                .build();
        return guestRepository.save(guest);
    }

    private Guest updateFromOcr(Guest existing, OcrFieldsPayload fields, String mrzLine1, String mrzLine2,
                                 boolean checkDigitsValid) {
        // Tamu lama scan ulang (mis. ganti paspor / kunjungan baru) - refresh data yang mungkin berubah.
        existing.setSurname(fields.getSurname());
        existing.setGivenNames(fields.getGivenNames());
        existing.setNationality(fields.getNationality());
        existing.setDateOfBirth(fields.getDateOfBirth());
        existing.setGender(Gender.valueOf(fields.getGender()));
        existing.setExpiryDate(fields.getExpiryDate());
        existing.setMrzLine1(mrzLine1);
        existing.setMrzLine2(mrzLine2);
        existing.setCheckDigitsValid(checkDigitsValid);
        return guestRepository.save(existing);
    }

    @Override
    @Transactional
    public Guest createFromManualEntry(GuestManualEntryRequest request) {
        String hash = HashUtil.sha256Hex(request.getPassportNumber());

        Guest guest = guestRepository.findByPassportNumberHashAndIssuingCountry(hash, request.getIssuingCountry())
                .orElseGet(Guest::new);

        guest.setDocumentType("P");
        guest.setPassportNumber(request.getPassportNumber());
        guest.setPassportNumberHash(hash);
        guest.setIssuingCountry(request.getIssuingCountry());
        guest.setSurname(request.getSurname());
        guest.setGivenNames(request.getGivenNames());
        guest.setNationality(request.getNationality());
        guest.setDateOfBirth(request.getDateOfBirth());
        guest.setGender(request.getGender());
        guest.setExpiryDate(request.getExpiryDate());
        // Input manual tidak melalui validasi checksum MRZ otomatis dari OCR.
        guest.setCheckDigitsValid(false);

        Guest saved = guestRepository.save(guest);
        linkScanLogIfPresent(request.getScanLogId(), saved);
        return saved;
    }

    private void linkScanLogIfPresent(Long scanLogId, Guest guest) {
        if (scanLogId == null) {
            return;
        }
        scanLogRepository.findById(scanLogId).ifPresent(log -> {
            log.setGuest(guest);
            log.setStatus(ScanStatus.MANUAL_CORRECTION);
            scanLogRepository.save(log);
        });
    }

    @Override
    public Guest getById(Long id) {
        return guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest dengan id " + id + " tidak ditemukan"));
    }
}
