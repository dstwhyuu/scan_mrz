"""
Verifikasi X-Internal-Api-Key header.
Endpoint OCR TIDAK boleh diekspos ke publik — hanya Spring Boot yang boleh memanggil.
"""

from fastapi import Header, HTTPException, status

from app.core.config import settings


async def verify_api_key(x_internal_api_key: str = Header(..., alias="X-Internal-Api-Key")) -> str:
    """
    FastAPI dependency: validasi bahwa request memiliki API key yang benar.
    Digunakan sebagai dependency di router agar semua endpoint internal terproteksi.
    """
    if x_internal_api_key != settings.INTERNAL_API_KEY:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="API key tidak valid",
        )
    return x_internal_api_key
