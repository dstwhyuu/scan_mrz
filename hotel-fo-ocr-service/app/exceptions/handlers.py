"""
Exception handler global — memastikan response error konsisten dalam format JSON.
"""

import logging

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

logger = logging.getLogger(__name__)


def register_exception_handlers(app: FastAPI) -> None:
    """Daftarkan custom exception handler ke FastAPI app."""

    @app.exception_handler(Exception)
    async def unhandled_exception_handler(request: Request, exc: Exception) -> JSONResponse:
        logger.error("Unhandled exception pada %s %s: %s", request.method, request.url.path, exc, exc_info=True)
        return JSONResponse(
            status_code=500,
            content={
                "success": False,
                "confidence": None,
                "mrzLine1": None,
                "mrzLine2": None,
                "checkDigitsValid": False,
                "fields": None,
                "errors": [f"Internal server error: {type(exc).__name__}"],
            },
        )
