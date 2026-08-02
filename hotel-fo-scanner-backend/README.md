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

## Yang BELUM Diimplementasikan (langkah selanjutnya)

- ⬜ `OcrClientService` — WebClient untuk memanggil FastAPI OCR service
- ⬜ `ScanOrchestratorService` — orkestrasi: terima upload gambar → panggil OCR → validasi → simpan Guest + GuestVisit + ScanLog
- ⬜ `GuestController` / `ScanController` — endpoint `POST /api/v1/scans`, `PUT /api/v1/guests/{id}`
- ⬜ `ExcelExportService` (Apache POI) — endpoint `GET /api/v1/reports/daily`
- ⬜ `ScanLogController` — audit trail untuk admin/supervisor
- ⬜ Unit test & integration test (Testcontainers)

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
terproteksi lain yang akan dibangun selanjutnya.

---

## Catatan Arsitektur

- `ddl-auto: validate` sengaja dipakai, bukan `update` — Hibernate hanya memverifikasi
  entity cocok dengan skema DB Anda, tidak mengubah skema secara otomatis.
- `PassportEncryptionConverter` di-load sebagai Spring Bean (`@Component`) agar encryption
  key bisa diambil dari `application.yml`, bukan di-hardcode di kelas converter.
- Password di-hash dengan BCrypt strength 12 — jangan diturunkan untuk alasan performa
  tanpa pertimbangan matang, ini data yang menjaga akses ke data paspor tamu.
