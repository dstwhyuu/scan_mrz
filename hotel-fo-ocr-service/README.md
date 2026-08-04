# Hotel FO OCR Service

Microservice FastAPI untuk ekstraksi data MRZ (Machine Readable Zone) dari gambar paspor.

## Prerequisites

- Python 3.11+
- [Tesseract OCR](https://github.com/UB-Mannheim/tesseract/wiki) terinstall di system PATH

## Setup

```bash
# 1. Buat virtual environment
python -m venv venv
venv\Scripts\activate        # Windows
# source venv/bin/activate   # Linux/Mac

# 2. Install dependencies
pip install -r requirements.txt

# 3. Copy env
cp .env.example .env
# Edit .env sesuai kebutuhan (terutama INTERNAL_API_KEY dan TESSERACT_CMD)

# 4. Jalankan
uvicorn app.main:app --reload --port 8000
```

## API Endpoint

| Method | Path | Fungsi |
|--------|------|--------|
| `POST` | `/internal/v1/ocr/extract-mrz` | Ekstrak MRZ dari gambar paspor |
| `GET`  | `/health` | Health check |
| `GET`  | `/docs` | Swagger UI |

### Header Wajib (untuk endpoint `/internal/*`)

```
X-Internal-Api-Key: <nilai INTERNAL_API_KEY di .env>
```

### Contoh Request (curl)

```bash
curl -X POST http://localhost:8000/internal/v1/ocr/extract-mrz \
  -H "X-Internal-Api-Key: dev-api-key-12345" \
  -F "image=@passport.jpg"
```
