"""
Parsing dan validasi MRZ (Machine Readable Zone) dari teks mentah OCR.
Menggunakan library `mrz` untuk parsing format ICAO 9303 + validasi checksum.
"""

import logging
import re
from datetime import date
from typing import Optional

from mrz.checker.td3 import TD3CodeChecker

from app.models.schemas import MrzExtractionResponse, OcrFields

logger = logging.getLogger(__name__)

# MRZ passport (TD3) terdiri dari 2 baris, masing-masing 44 karakter
MRZ_LINE_LENGTH = 44
MRZ_LINE_PATTERN = re.compile(r"[A-Z0-9<]{30,44}")


def parse_mrz(raw_text: str, confidence: float) -> MrzExtractionResponse:
    """
    Parse teks mentah OCR menjadi data MRZ terstruktur.

    Flow:
    1. Cari 2 baris MRZ dari teks OCR
    2. Parse dengan library `mrz`
    3. Validasi checksum
    4. Kembalikan response terstruktur
    """
    lines = _extract_mrz_lines(raw_text)

    if len(lines) < 2:
        logger.warning("Tidak bisa menemukan 2 baris MRZ dari teks OCR")
        return MrzExtractionResponse(
            success=False,
            confidence=confidence,
            errors=["MRZ zone not detected — tidak ditemukan 2 baris MRZ yang valid"],
        )

    mrz_line1 = _normalize_line(lines[0])
    mrz_line2 = _normalize_line(lines[1])

    logger.info("MRZ Line 1: %s", mrz_line1)
    logger.info("MRZ Line 2: %s", mrz_line2)

    try:
        checker = TD3CodeChecker(f"{mrz_line1}\n{mrz_line2}")
        check_digits_valid = bool(checker.result)

        fields = _extract_fields(checker, mrz_line1, mrz_line2)

        return MrzExtractionResponse(
            success=True,
            confidence=confidence,
            mrzLine1=mrz_line1,
            mrzLine2=mrz_line2,
            checkDigitsValid=check_digits_valid,
            fields=fields,
            errors=[] if check_digits_valid else ["Check digit validation failed"],
        )

    except Exception as e:
        logger.error("MRZ parsing error: %s", e)
        return MrzExtractionResponse(
            success=False,
            confidence=confidence,
            mrzLine1=mrz_line1,
            mrzLine2=mrz_line2,
            checkDigitsValid=False,
            fields=_try_manual_parse(mrz_line1, mrz_line2),
            errors=[f"MRZ parsing error: {str(e)}"],
        )


def _extract_mrz_lines(raw_text: str) -> list[str]:
    """Cari baris-baris yang cocok dengan pola MRZ dari teks mentah OCR."""
    candidates = []
    for line in raw_text.split("\n"):
        line = line.strip()
        if MRZ_LINE_PATTERN.search(line) and len(line) >= 30:
            candidates.append(line)

    # Ambil 2 baris terakhir yang cocok (MRZ selalu di bagian bawah)
    return candidates[-2:] if len(candidates) >= 2 else candidates


def _normalize_line(line: str) -> str:
    """Normalisasi baris MRZ: hapus spasi, pad/trim ke 44 karakter."""
    # Hapus karakter yang bukan A-Z, 0-9, <
    cleaned = re.sub(r"[^A-Z0-9<]", "", line.upper())

    # Pad dengan '<' jika kurang dari 44 karakter
    if len(cleaned) < MRZ_LINE_LENGTH:
        cleaned = cleaned.ljust(MRZ_LINE_LENGTH, "<")

    # Trim jika lebih dari 44 karakter
    return cleaned[:MRZ_LINE_LENGTH]


def _extract_fields(checker: TD3CodeChecker, line1: str, line2: str) -> OcrFields:
    """Ekstrak field-field terstruktur dari hasil parsing MRZ."""
    fields = checker.fields()

    return OcrFields(
        documentType=_safe_get(fields, "document_type", line1[0] if line1 else None),
        issuingCountry=_safe_get(fields, "country", line1[2:5].replace("<", "") if line1 else None),
        surname=_safe_get(fields, "surname"),
        givenNames=_safe_get(fields, "name"),
        passportNumber=_safe_get(fields, "document_number"),
        nationality=_safe_get(fields, "nationality"),
        dateOfBirth=_format_date(_safe_get(fields, "birth_date")),
        gender=_map_gender(_safe_get(fields, "sex")),
        expiryDate=_format_date(_safe_get(fields, "expiry_date")),
    )


def _try_manual_parse(line1: str, line2: str) -> Optional[OcrFields]:
    """Fallback: parse manual jika library mrz gagal."""
    try:
        # Line 1: P<CCCSURNAME<<GIVENNAMES<<<...
        doc_type = line1[0] if line1 else None
        issuing_country = line1[2:5].replace("<", "") if len(line1) > 4 else None

        name_part = line1[5:] if len(line1) > 5 else ""
        name_parts = name_part.split("<<", 1)
        surname = name_parts[0].replace("<", " ").strip() if name_parts else None
        given_names = name_parts[1].replace("<", " ").strip() if len(name_parts) > 1 else None

        # Line 2: PPNNNNNNNcCCCYYMMDDcSYYMMDDc...
        passport_number = line2[0:9].replace("<", "").strip() if len(line2) > 9 else None
        nationality = line2[10:13].replace("<", "") if len(line2) > 12 else None

        dob_raw = line2[13:19] if len(line2) > 18 else None
        gender_raw = line2[20] if len(line2) > 20 else None
        expiry_raw = line2[21:27] if len(line2) > 26 else None

        return OcrFields(
            documentType=doc_type,
            issuingCountry=issuing_country,
            surname=surname,
            givenNames=given_names,
            passportNumber=passport_number,
            nationality=nationality,
            dateOfBirth=_format_date_raw(dob_raw),
            gender=_map_gender(gender_raw),
            expiryDate=_format_date_raw(expiry_raw),
        )
    except Exception as e:
        logger.error("Manual MRZ parse failed: %s", e)
        return None


def _safe_get(fields: dict, key: str, fallback=None) -> Optional[str]:
    """Ambil value dari dict, return fallback jika key tidak ada atau kosong."""
    val = fields.get(key)
    if val and str(val).strip():
        return str(val).strip().replace("<", " ").strip()
    return fallback


def _map_gender(raw: Optional[str]) -> Optional[str]:
    """Map gender dari MRZ ke format yang dipakai entity (M/F/X)."""
    if not raw:
        return None
    raw = raw.strip().upper()
    if raw in ("M", "F"):
        return raw
    return "X"


def _format_date(raw: Optional[str]) -> Optional[str]:
    """Format tanggal dari library mrz ke YYYY-MM-DD."""
    if not raw:
        return None
    try:
        # Library mrz biasanya mengembalikan objek date atau string
        if isinstance(raw, date):
            return raw.isoformat()
        # Coba parse format YYMMDD
        return _format_date_raw(raw)
    except Exception:
        return str(raw)


def _format_date_raw(raw: Optional[str]) -> Optional[str]:
    """
    Format tanggal mentah YYMMDD ke YYYY-MM-DD.
    Asumsi: tahun 00-29 = 2000-2029, tahun 30-99 = 1930-1999.
    """
    if not raw or len(raw) < 6:
        return None
    try:
        yy = int(raw[0:2])
        mm = int(raw[2:4])
        dd = int(raw[4:6])
        year = 2000 + yy if yy < 30 else 1900 + yy
        return f"{year:04d}-{mm:02d}-{dd:02d}"
    except (ValueError, IndexError):
        return None
