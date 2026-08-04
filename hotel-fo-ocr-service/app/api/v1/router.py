"""
Router aggregator untuk semua endpoint v1.
"""

from fastapi import APIRouter, Depends

from app.api.v1.endpoints import mrz
from app.core.security import verify_api_key

# Semua endpoint di bawah prefix /internal/v1/ocr wajib menyertakan X-Internal-Api-Key
router = APIRouter(
    prefix="/internal/v1/ocr",
    tags=["OCR"],
    dependencies=[Depends(verify_api_key)],
)

router.include_router(mrz.router)
