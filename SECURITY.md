# Security Policy

## Supported Versions

Use this section to tell people about which versions of your project are currently being supported with security updates.

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

Kami sangat menghargai laporan kerentanan keamanan dari komunitas. Untuk melaporkan kerentanan keamanan:

### Email Security
Kirim email ke: security@kasirkaskecil.com

### Format Laporan
Mohon sertakan informasi berikut dalam laporan Anda:

1. **Deskripsi Kerentanan**
   - Jelaskan kerentanan dengan detail
   - Sertakan langkah reproduksi
   - Jelaskan dampak potensial

2. **Informasi Teknis**
   - Versi aplikasi yang terpengaruh
   - Platform dan versi Android
   - Device yang digunakan

3. **Proof of Concept**
   - Kode atau script untuk reproduksi
   - Screenshot jika relevan
   - Log error jika ada

4. **Kontak**
   - Nama Anda (opsional)
   - Email untuk follow-up
   - Preferensi untuk credit dalam advisories

### Timeline Response
- **24 jam**: Konfirmasi penerimaan laporan
- **72 jam**: Update status investigasi
- **7 hari**: Update progress dan timeline fix
- **30 hari**: Release patch atau advisory

### Disclosure Policy
- Kami akan mengkoordinasikan disclosure dengan reporter
- Credit akan diberikan kepada reporter (jika diinginkan)
- Advisory akan dipublikasikan setelah patch tersedia
- Timeline disclosure akan disesuaikan dengan severity

## Security Best Practices

### For Developers
- Jangan commit sensitive data (API keys, passwords)
- Validasi semua user input
- Implementasikan proper authentication
- Gunakan HTTPS untuk network calls
- Update dependencies secara regular
- Review code untuk security issues

### For Users
- Update aplikasi secara regular
- Jangan share credentials
- Gunakan device yang aman
- Backup data secara regular
- Report suspicious activities

## Security Features

### Authentication
- Password-based authentication
- Role-based access control
- Session management
- Auto-logout functionality

### Data Protection
- Local database encryption
- Secure password storage
- Input validation
- SQL injection prevention

### Network Security
- HTTPS for all network calls
- Certificate pinning
- Secure API endpoints
- Data encryption in transit

## Responsible Disclosure

Kami berkomitmen untuk:
- Mengakui laporan kerentanan dengan cepat
- Menyediakan timeline yang jelas
- Memberikan credit kepada reporter
- Memperbaiki kerentanan secara tepat waktu
- Mengkomunikasikan status secara transparan

## Contact

Untuk pertanyaan keamanan:
- Email: security@kasirkaskecil.com
- PGP Key: [Available upon request]
- Response time: Within 24 hours

---

Terima kasih atas kontribusi Anda untuk keamanan Kasir Kas Kecil! 🔒 