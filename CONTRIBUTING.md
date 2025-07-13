# Contributing to Kasir Kas Kecil

Terima kasih atas minat Anda untuk berkontribusi pada proyek Kasir Kas Kecil! Dokumen ini berisi panduan untuk berkontribusi pada proyek ini.

## 🚀 Cara Berkontribusi

### 1. Fork dan Clone Repository

1. Fork repository ini ke akun GitHub Anda
2. Clone repository yang sudah di-fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/kasir-kas-kecil.git
   cd kasir-kas-kecil
   ```

### 2. Setup Development Environment

1. Pastikan Anda memiliki:
   - Android Studio Arctic Fox atau lebih baru
   - Android SDK API 24+
   - Java 8 atau lebih baru

2. Buka project di Android Studio
3. Sync Gradle files
4. Build project untuk memastikan tidak ada error

### 3. Membuat Branch

Buat branch baru untuk fitur atau bug fix:
```bash
git checkout -b feature/nama-fitur
# atau
git checkout -b fix/nama-bug
```

### 4. Development Guidelines

#### Code Style
- Gunakan Java coding conventions
- Indentasi 4 spasi
- Nama class menggunakan PascalCase
- Nama method dan variable menggunakan camelCase
- Nama constant menggunakan UPPER_SNAKE_CASE

#### Architecture
- Ikuti MVVM pattern yang sudah ada
- Gunakan Repository pattern untuk data access
- Implementasikan ViewModel untuk business logic
- Gunakan LiveData untuk reactive programming

#### Database
- Tambahkan migration jika mengubah schema
- Gunakan Room annotations dengan benar
- Implementasikan DAO untuk setiap entity

#### UI/UX
- Ikuti Material Design guidelines
- Pastikan responsive untuk tablet dan phone
- Gunakan dimens dan colors dari resources
- Implementasikan loading states dan error handling

### 5. Testing

Sebelum submit PR, pastikan:
- [ ] Code dapat di-compile tanpa error
- [ ] Tidak ada lint warnings
- [ ] Fitur berfungsi sesuai requirement
- [ ] UI responsive di berbagai ukuran layar
- [ ] Database operations berfungsi dengan baik

### 6. Commit Messages

Gunakan format commit message yang jelas:
```
type(scope): description

[optional body]

[optional footer]
```

Contoh:
```
feat(product): add product search functionality

- Implement search by name and category
- Add search suggestions
- Update product list UI

Closes #123
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance tasks

### 7. Pull Request

1. Push branch ke repository Anda
2. Buat Pull Request ke repository utama
3. Isi template PR dengan lengkap:
   - Deskripsi perubahan
   - Screenshot (jika ada perubahan UI)
   - Testing yang sudah dilakukan
   - Checklist yang sudah diselesaikan

## 📋 Issue Guidelines

### Bug Report
- Jelaskan bug dengan detail
- Sertakan langkah reproduksi
- Screenshot jika diperlukan
- Informasi device dan Android version

### Feature Request
- Jelaskan fitur yang diinginkan
- Sertakan use case
- Mockup atau wireframe jika ada
- Prioritas fitur

## 🏗️ Project Structure

```
app/src/main/java/id/tugas/pos/
├── data/
│   ├── model/           # Entity classes
│   ├── database/        # Room Database & DAOs
│   └── repository/      # Repository classes
├── ui/
│   ├── login/          # Login screen
│   ├── dashboard/      # Dashboard screen
│   ├── product/        # Product management
│   ├── transaction/    # POS transaction
│   ├── history/        # Transaction history
│   ├── expense/        # Expense management
│   ├── report/         # Reports & analytics
│   └── settings/       # App settings
├── viewmodel/          # ViewModel classes
├── utils/              # Utility classes
└── worker/             # Background workers
```

## 🧪 Testing

### Unit Testing
- Test ViewModel logic
- Test Repository methods
- Test utility functions

### UI Testing
- Test Activity navigation
- Test Fragment interactions
- Test user input validation

### Integration Testing
- Test database operations
- Test API integrations
- Test printer functionality

## 📚 Documentation

- Update README.md jika ada perubahan setup
- Update CHANGELOG.md untuk setiap release
- Dokumentasikan API baru
- Tambahkan komentar untuk kode kompleks

## 🔒 Security

- Jangan commit sensitive data (API keys, passwords)
- Validasi semua user input
- Implementasikan proper authentication
- Gunakan HTTPS untuk network calls

## 📞 Getting Help

Jika Anda mengalami kesulitan:
1. Cek dokumentasi di README.md
2. Cari issue yang sudah ada
3. Buat issue baru dengan label `question`
4. Hubungi maintainer melalui email

## 🎉 Recognition

Kontributor akan ditampilkan di:
- README.md contributors section
- Release notes
- Project documentation

---

Terima kasih atas kontribusi Anda untuk membuat Kasir Kas Kecil lebih baik! 🚀 