"""
Endpoint: POST /internal/v1/ocr/extract-mrz
Terima gambar paspor, proses OCR + parsing MRZ, kembalikan data terstruktur.
"""

import logging

from fastapi import APIRouter, File, UploadFile

from app.models.schemas import MrzExtractionResponse
from app.services import image_preprocessor, mrz_parser, ocr_engine

logger = logging.getLogger(__name__)

router = APIRouter()


@router.post(
    "/extract-mrz",
    response_model=MrzExtractionResponse,
    summary="Ekstrak data MRZ dari gambar paspor",
    description="Terima gambar paspor (JPG/PNG), proses OCR + parsing MRZ, "
                "kembalikan data terstruktur. Selalu return 200 — error bisnis "
                "ditandai dengan success=false.",
)
async def extract_mrz(image: UploadFile = File(..., description="Gambar paspor (JPG/PNG)")) -> MrzExtractionResponse:
    logger.info("Menerima request extract-mrz: filename=%s, content_type=%s", image.filename, image.content_type)

    # 1. Baca bytes dari uploaded file
    image_bytes = await image.read()

    if not image_bytes:
        return MrzExtractionResponse(
            success=False,
            errors=["File gambar kosong"],
        )

    # 2. Preprocess gambar (crop MRZ region)
    try:
        processed_image = image_preprocessor.preprocess_image(image_bytes)
    except ValueError as e:
        logger.error("Image preprocessing failed: %s", e)
        return MrzExtractionResponse(
            success=False,
            errors=[str(e)],
        )

    # 3. OCR: ekstrak teks + hitung confidence
    raw_text = ocr_engine.extract_text(processed_image)
    confidence = ocr_engine.get_ocr_confidence(processed_image)

    logger.info("OCR selesai: confidence=%.2f%%, text_length=%d", confidence, len(raw_text))

    # 4. Jika crop MRZ gagal mendeteksi teks, coba full page
    if not raw_text.strip() or len(raw_text.strip()) < 20:
        logger.info("MRZ crop tidak mendeteksi teks, mencoba full page...")
        try:
            full_page_image = image_preprocessor.preprocess_full_page(image_bytes)
            raw_text = ocr_engine.extract_text(full_page_image)
            confidence = ocr_engine.get_ocr_confidence(full_page_image)
        except ValueError:
            pass  # Tetap pakai hasil sebelumnya

    if not raw_text.strip():
        return MrzExtractionResponse(
            success=False,
            confidence=confidence,
            errors=["Tidak ada teks yang terdeteksi dari gambar"],
        )

    # 5. Parse MRZ dari teks OCR
    result = mrz_parser.parse_mrz(raw_text, confidence)

    logger.info("MRZ parsing selesai: success=%s, checkDigitsValid=%s", result.success, result.checkDigitsValid)
    return result
