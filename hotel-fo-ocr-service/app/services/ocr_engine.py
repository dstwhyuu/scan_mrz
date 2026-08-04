"""
Wrapper Tesseract OCR — ekstrak teks mentah dari gambar yang sudah dipreproses.
"""

import logging

import numpy as np
import pytesseract

from app.core.config import settings

logger = logging.getLogger(__name__)

# Set path ke binary Tesseract
pytesseract.pytesseract.tesseract_cmd = settings.TESSERACT_CMD

# Config Tesseract untuk MRZ:
# --psm 6  : Assume a single uniform block of text (cocok untuk MRZ yang sudah di-crop)
# --oem 3  : Default OCR Engine Mode (LSTM + legacy)
# -c tessedit_char_whitelist : Batasi karakter ke yang valid di MRZ
MRZ_VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789<"
TESSERACT_CONFIG = f"--psm 6 --oem 3 -c tessedit_char_whitelist={MRZ_VALID_CHARS}"


def extract_text(processed_image: np.ndarray) -> str:
    """
    Jalankan Tesseract OCR pada gambar yang sudah dipreproses.
    Return raw text hasil OCR.
    """
    raw_text = pytesseract.image_to_string(processed_image, config=TESSERACT_CONFIG)
    cleaned = raw_text.strip()
    logger.debug("OCR raw output (%d chars): %s", len(cleaned), cleaned[:100])
    return cleaned


def get_ocr_confidence(processed_image: np.ndarray) -> float:
    """
    Hitung rata-rata confidence score dari Tesseract.
    Return nilai 0-100.
    """
    data = pytesseract.image_to_data(processed_image, config=TESSERACT_CONFIG, output_type=pytesseract.Output.DICT)

    confidences = [
        int(conf) for conf, text in zip(data["conf"], data["text"])
        if int(conf) > 0 and text.strip()  # abaikan entry kosong / confidence -1
    ]

    if not confidences:
        return 0.0

    avg_confidence = sum(confidences) / len(confidences)
    logger.debug("OCR confidence: %.2f%% (%d words)", avg_confidence, len(confidences))
    return round(avg_confidence, 2)
