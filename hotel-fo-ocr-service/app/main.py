"""
Hotel FO OCR Service — FastAPI entrypoint.

Service stateless untuk ekstraksi data MRZ dari gambar paspor.
Hanya bisa diakses secara internal oleh Spring Boot via X-Internal-Api-Key.
"""

import logging

from fastapi import FastAPI

from app.api.v1.router import router as v1_router
from app.core.config import settings
from app.core.logging_config import setup_logging
from app.exceptions.handlers import register_exception_handlers

# Setup logging sebelum hal lain
setup_logging()

logger = logging.getLogger(__name__)

app = FastAPI(
    title="Hotel FO OCR Service",
    description="Stateless microservice untuk ekstraksi data MRZ dari gambar paspor. "
                "Hanya bisa diakses secara internal oleh Spring Boot backend.",
    version="0.1.0",
    docs_url="/docs",       # Swagger UI untuk development
    redoc_url=None,
)

# Register exception handlers
register_exception_handlers(app)

# Register routers
app.include_router(v1_router)


@app.get("/health", tags=["Health"])
async def health_check():
    """Health check endpoint — tidak dilindungi API key."""
    return {"status": "ok", "service": "hotel-fo-ocr-service"}


if __name__ == "__main__":
    import uvicorn

    logger.info("Starting OCR service on %s:%d", settings.HOST, settings.PORT)
    uvicorn.run(
        "app.main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=True,
    )
