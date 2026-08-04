"""
Preprocessing gambar paspor sebelum di-OCR oleh Tesseract.
Tujuan: meningkatkan akurasi pembacaan MRZ yang terletak di bagian bawah halaman paspor.

Pipeline:
  1. Decode bytes → OpenCV image
  2. Grayscale
  3. Resize (jika terlalu kecil)
  4. Denoise
  5. Adaptive threshold (binarisasi)
  6. Crop area MRZ (opsional, jika bisa dideteksi)
"""

import logging

import cv2
import numpy as np

logger = logging.getLogger(__name__)

# MRZ biasanya menempati ±15-20% bagian bawah halaman paspor
MRZ_BOTTOM_RATIO = 0.25
MIN_WIDTH_FOR_OCR = 1000


def preprocess_image(image_bytes: bytes) -> np.ndarray:
    """
    Terima raw bytes dari gambar, kembalikan image yang sudah diproses
    dan siap di-OCR oleh Tesseract.
    """
    # 1. Decode
    nparr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    if img is None:
        raise ValueError("Gagal decode gambar — format tidak didukung atau file rusak")

    logger.debug("Image decoded: %dx%d", img.shape[1], img.shape[0])

    # 2. Grayscale
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    # 3. Resize jika terlalu kecil (resolusi rendah = OCR buruk)
    h, w = gray.shape
    if w < MIN_WIDTH_FOR_OCR:
        scale = MIN_WIDTH_FOR_OCR / w
        gray = cv2.resize(gray, None, fx=scale, fy=scale, interpolation=cv2.INTER_CUBIC)
        h, w = gray.shape
        logger.debug("Image resized to %dx%d", w, h)

    # 4. Crop bagian bawah (area MRZ)
    mrz_region = _crop_mrz_region(gray)

    # 5. Denoise
    denoised = cv2.fastNlMeansDenoising(mrz_region, h=10)

    # 6. Adaptive threshold (binarisasi untuk kontras maksimal)
    binary = cv2.adaptiveThreshold(
        denoised, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 31, 15
    )

    return binary


def preprocess_full_page(image_bytes: bytes) -> np.ndarray:
    """
    Fallback: proses seluruh halaman tanpa crop MRZ.
    Dipakai jika crop MRZ gagal mendeteksi teks yang valid.
    """
    nparr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    if img is None:
        raise ValueError("Gagal decode gambar — format tidak didukung atau file rusak")

    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    h, w = gray.shape
    if w < MIN_WIDTH_FOR_OCR:
        scale = MIN_WIDTH_FOR_OCR / w
        gray = cv2.resize(gray, None, fx=scale, fy=scale, interpolation=cv2.INTER_CUBIC)

    denoised = cv2.fastNlMeansDenoising(gray, h=10)
    binary = cv2.adaptiveThreshold(
        denoised, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 31, 15
    )

    return binary


def _crop_mrz_region(gray: np.ndarray) -> np.ndarray:
    """Ambil bagian bawah gambar di mana MRZ biasanya berada."""
    h, w = gray.shape
    mrz_top = int(h * (1 - MRZ_BOTTOM_RATIO))
    cropped = gray[mrz_top:h, 0:w]
    logger.debug("MRZ region cropped: y=%d to y=%d", mrz_top, h)
    return cropped
