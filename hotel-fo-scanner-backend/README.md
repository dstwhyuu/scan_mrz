# Hotel FO Scanner — Backend (Spring Boot)

Implementasi tahap 1: **project setup + entity layer + security/JWT authentication end-to-end**.
Guest scanning flow (integrasi ke FastAPI OCR) dan Excel report belum diimplementasikan —
lihat bagian "Langkah Selanjutnya" di bawah.

---

## ⚠️ WAJIB: Patch skema database Anda dulu sebelum menjalankan aplikasi ini

Di cetak biru awal, kolom `passport_number` didefinisikan `VARCHAR(20)`. Tapi karena
`Guest.java` mengenkripsi kolom ini (AES-256-GCM) sebelum disimpan, hasil ciphertext yang
di-base64 akan **jauh lebih panjang** dari 20 karakter (±60-70 karakter untuk nomor paspor
9 digit). Jalankan ini di database yang sudah Anda buat:

```sql
ALTER TABLE guests MODIFY COLUMN passport_number VARCHAR(255) NOT NULL;
```

Tanpa patch ini, insert data tamu pertama akan gagal dengan `Data too long for column`.

---

## Yang Sudah Diimplementasikan

- ✅ Entity `User`, `Guest`, `GuestVisit`, `ScanLog` — 1:1 mapping ke DDL yang sudah Anda buat
- ✅ Repository layer (Spring Data JPA)
- ✅ **Enkripsi kolom PII** (`passportNumber`) via `PassportEncryptionConverter` (AES-256-GCM)
- ✅ **Hashing** (`HashUtil`, SHA-256) untuk pencarian nomor paspor tanpa membuka enkripsi
- ✅ **JWT Authentication penuh**: `JwtTokenProvider`, `JwtAuthenticationFilter`, `SecurityConfig`
- ✅ Account lockout otomatis setelah 5x percobaan login gagal (15 menit)
- ✅ `POST /api/v1/auth/login` dan `POST /api/v1/auth/refresh` — sudah bisa langsung dites
- ✅ `GlobalExceptionHandler` — response error konsisten di semua endpoint
- ✅ Dev data seeder — otomatis membuat akun admin saat pertama kali start di profile `dev`

- ✅ **Alur scan lengkap**: `POST /api/v1/scans` — upload gambar → `OcrClientService` panggil FastAPI (RestClient, bukan WebClient) → evaluasi confidence & checksum → simpan `Guest` + `ScanLog`, atau kembalikan data parsial untuk dikoreksi
- ✅ `POST /api/v1/guests` — input manual / koreksi hasil scan low-confidence (bisa di-link ke `scanLogId` asal)
- ✅ `GET /api/v1/guests/{id}`

## Yang BELUM Diimplementasikan (langkah selanjutnya)

- ⬜ `GuestVisitService` / endpoint check-in (assign kamar, tanggal check-in/out) — scan flow saat ini hanya membuat identitas `Guest`, belum membuat `GuestVisit`
- ⬜ `ExcelExportService` (Apache POI) — endpoint `GET /api/v1/reports/daily`
- ⬜ `ScanLogController` — audit trail untuk admin/supervisor
- ⬜ Unit test & integration test (Testcontainers)
- ⬜ **FastAPI OCR service itu sendiri** — endpoint `POST /internal/v1/ocr/extract-mrz` di kode Python belum dibuat; Spring Boot sudah siap memanggilnya begitu service itu jalan di `OCR_SERVICE_URL`

### ⚠️ Belum bisa dikompilasi/dites di sandbox ini

Sandbox saya tidak punya akses ke Maven Central, jadi kode di atas **belum pernah di-`mvn compile`**.
Tolong jalankan `mvn compile` di mesin Anda dan kirim balik pesan errornya kalau ada — biasanya cuma
typo import atau versi dependency yang perlu disesuaikan.

---

## Cara Menjalankan (Local Development)

### 1. Set environment variable

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=hotel_fo_scanner
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
```

> Untuk profile `dev`, `JWT_SECRET` dan `PASSPORT_ENCRYPTION_KEY` **sudah otomatis di-set**
> lewat `application-dev.yml` — Anda tidak perlu export manual saat development lokal.
> Untuk staging/production, WAJIB generate baru dan simpan di secret manager:
> ```bash
> openssl rand -base64 48   # untuk JWT_SECRET
> openssl rand -base64 32   # untuk PASSPORT_ENCRYPTION_KEY
> ```

### 2. Jalankan dengan profile `dev`

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Saat startup pertama kali, akan muncul log:
```
>>> Dev admin user seeded. username=admin | password=Admin@12345
```

### 3. Test endpoint login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@12345"}'
```

Response yang diharapkan:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "username": "admin",
    "fullName": "Administrator",
    "role": "ADMIN"
  }
}
```

Gunakan `accessToken` di header `Authorization: Bearer <token>` untuk mengakses endpoint
terproteksi lain, termasuk dua contoh di bawah.

### 4. Test endpoint scan (butuh FastAPI OCR service sudah jalan)

```bash
curl -X POST http://localhost:8080/api/v1/scans \
  -H "Authorization: Bearer <accessToken>" \
  -F "passportImage=@/path/ke/foto-paspor.jpg"
```

- Response `200 OK` jika confidence tinggi & checksum valid → `Guest` otomatis tersimpan.
- Response `422 Unprocessable Entity` jika confidence rendah → `requiresManualReview: true`,
  gunakan `partialData` untuk prefill form koreksi lalu kirim ke endpoint di bawah.

### 5. Test endpoint input/koreksi manual

```bash
curl -X POST http://localhost:8080/api/v1/guests \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "scanLogId": 12,
    "passportNumber": "A1234567",
    "issuingCountry": "IDN",
    "surname": "SANTOSO",
    "givenNames": "BUDI",
    "nationality": "IDN",
    "dateOfBirth": "1990-05-10",
    "gender": "M",
    "expiryDate": "2029-05-10"
  }'
```

`scanLogId` opsional — isi jika ini koreksi dari scan yang gagal/low-confidence, agar
`scan_log` terkait ter-link ke `Guest` yang baru dibuat dan statusnya berubah jadi
`MANUAL_CORRECTION`.

### Konfigurasi tambahan untuk scan flow

```bash
export OCR_SERVICE_URL=http://localhost:8000
export OCR_INTERNAL_API_KEY=<samakan dengan API key di FastAPI>
```

---

## Catatan Arsitektur

- `ddl-auto: validate` sengaja dipakai, bukan `update` — Hibernate hanya memverifikasi
  entity cocok dengan skema DB Anda, tidak mengubah skema secara otomatis.
- `PassportEncryptionConverter` di-load sebagai Spring Bean (`@Component`) agar encryption
  key bisa diambil dari `application.yml`, bukan di-hardcode di kelas converter.
- Password di-hash dengan BCrypt strength 12 — jangan diturunkan untuk alasan performa
  tanpa pertimbangan matang, ini data yang menjaga akses ke data paspor tamu.
