"""
Pydantic models — kontrak response yang HARUS sinkron dengan
OcrExtractionResponse / OcrFieldsPayload di Spring Boot.
"""

from pydantic import BaseModel
from typing import Optional


class OcrFields(BaseModel):
    """Field MRZ yang sudah diparsing. Sinkron dengan OcrFieldsPayload.java"""
    documentType: Optional[str] = None
    issuingCountry: Optional[str] = None
    surname: Optional[str] = None
    givenNames: Optional[str] = None
    passportNumber: Optional[str] = None
    nationality: Optional[str] = None
    dateOfBirth: Optional[str] = None  # format: YYYY-MM-DD
    gender: Optional[str] = None       # M / F / X
    expiryDate: Optional[str] = None   # format: YYYY-MM-DD


class MrzExtractionResponse(BaseModel):
    """
    Response utama endpoint extract-mrz.
    Sinkron dengan OcrExtractionResponse.java di Spring Boot.

    Catatan: error OCR tetap dikembalikan sebagai 200 OK dengan success=false,
    bukan HTTP error — agar Spring Boot bisa menangani sebagai business flow.
    """
    success: bool
    confidence: Optional[float] = None
    mrzLine1: Optional[str] = None
    mrzLine2: Optional[str] = None
    checkDigitsValid: bool = False
    fields: Optional[OcrFields] = None
    errors: list[str] = []
