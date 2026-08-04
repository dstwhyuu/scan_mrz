"""
Konfigurasi aplikasi via environment variables.
Menggunakan Pydantic Settings agar validasi otomatis saat startup.
"""

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    # API Key untuk validasi request dari Spring Boot
    INTERNAL_API_KEY: str = "changeme"

    # Path ke binary Tesseract OCR
    TESSERACT_CMD: str = r"C:\Program Files\Tesseract-OCR\tesseract.exe"

    # Logging
    LOG_LEVEL: str = "INFO"

    # Server
    HOST: str = "0.0.0.0"
    PORT: int = 8000


settings = Settings()
