# Kasir Kas Kecil - Android POS Application

Aplikasi Point of Sale (POS) Android yang dirancang untuk usaha kecil dan menengah dengan fitur lengkap untuk manajemen transaksi, produk, dan laporan bisnis.

## 🚀 Fitur Utama

### 🔐 Sistem Autentikasi
- **Role-based Authentication**: Admin dan User
- **Login System**: Username dan password
- **Session Management**: Auto-logout dan session tracking

### 📊 Dashboard
- **Real-time Metrics**: Pendapatan, penjualan hari ini, total produk
- **Stock Monitoring**: Alert stok menipis dan habis
- **Quick Actions**: Akses cepat ke fitur utama
- **Profit Analysis**: Analisis margin keuntungan

### 🛍️ Manajemen Produk
- **Product Management**: CRUD operasi untuk produk
- **Category Management**: Pengelolaan kategori produk
- **Stock Management**: Monitoring stok real-time
- **Barcode Support**: Scanner barcode untuk produk
- **Image Support**: Upload dan manajemen gambar produk

### 💰 Transaksi POS
- **Sales Transaction**: Proses penjualan dengan interface yang mudah
- **Payment Methods**: Tunai, kartu, transfer
- **Receipt Printing**: Cetak struk menggunakan printer thermal
- **Discount & Tax**: Perhitungan diskon dan pajak
- **Change Calculation**: Perhitungan kembalian otomatis

### 📋 Riwayat Transaksi
- **Transaction History**: Riwayat lengkap semua transaksi
- **Search & Filter**: Pencarian dan filter berdasarkan tanggal, status, kasir
- **Transaction Details**: Detail lengkap setiap transaksi
- **Receipt Reprint**: Cetak ulang struk

### 💸 Manajemen Pengeluaran
- **Expense Tracking**: Pencatatan pengeluaran bisnis
- **Category Management**: Kategori pengeluaran (Operasional, Utilitas, Gaji, dll)
- **Receipt Attachment**: Lampiran bukti pengeluaran
- **Expense Reports**: Laporan pengeluaran

### 📈 Laporan dan Analisis
- **Sales Reports**: Laporan penjualan harian, mingguan, bulanan
- **Profit Reports**: Analisis keuntungan dan margin
- **Inventory Reports**: Laporan stok dan inventori
- **Export Features**: Ekspor ke PDF dan Excel
- **Chart Visualization**: Grafik dan visualisasi data

### ⚙️ Pengaturan
- **Printer Configuration**: Pengaturan printer thermal
- **Company Information**: Informasi perusahaan
- **Tax Settings**: Konfigurasi pajak
- **Backup & Restore**: Backup dan restore data
- **User Management**: Manajemen user (Admin only)

## 🏗️ Arsitektur

### MVVM Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     View        │    │   ViewModel     │    │     Model       │
│   (Activity/    │◄──►│   (ViewModel)   │◄──►│   (Repository)  │
│   Fragment)     │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Package Structure
```
id.tugas.pos/
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

## 🛠️ Teknologi yang Digunakan

### Core Technologies
- **Language**: Java
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room Database (SQLite)
- **UI Framework**: Material Design Components
- **Navigation**: Navigation Component
- **Image Loading**: Glide
- **PDF Generation**: iText7
- **Date/Time**: ThreeTenABP

### Libraries
- **Room**: Local database
- **LiveData**: Reactive programming
- **ViewModel**: Lifecycle-aware components
- **Material Design**: UI components
- **ESCPOS-ThermalPrinter**: Thermal printer support
- **WorkManager**: Background tasks

## 📱 UI/UX Features

### Responsive Design
- **Phone Layout**: Optimized for mobile devices
- **Tablet Layout**: Enhanced layout for tablets (sw600dp, sw720dp)
- **Landscape Support**: Responsive landscape orientation
- **Material Design**: Modern Material Design 3 components

### Navigation
- **Navigation Drawer**: Side navigation menu
- **Bottom Navigation**: Quick access on tablets
- **Fragment-based**: Modular screen management
- **Deep Linking**: Direct navigation to specific screens

## 🔧 Setup dan Instalasi

### Prerequisites
- Android Studio Arctic Fox atau lebih baru
- Android SDK API 24+ (Android 7.0)
- Java 8 atau lebih baru

### Installation Steps
1. Clone repository ini
2. Buka project di Android Studio
3. Sync Gradle files
4. Build project
5. Run di device atau emulator

### Default Credentials
```
Admin:
- Username: admin
- Password: admin123

User:
- Username: user
- Password: user123
```

## 🖨️ Printer Thermal Setup

### Supported Printers
- ESC/POS compatible thermal printers
- Bluetooth thermal printers
- USB thermal printers

### Configuration
1. Buka Settings > Printer Configuration
2. Pilih jenis koneksi (Bluetooth/USB)
3. Test koneksi printer
4. Konfigurasi format struk

## 📊 Database Schema

### Tables
- **users**: User management dan authentication
- **products**: Product catalog dan inventory
- **transactions**: Sales transactions
- **transaction_items**: Individual items in transactions
- **expenses**: Business expenses

### Relationships
- One-to-Many: Transaction → TransactionItems
- One-to-Many: User → Transactions
- One-to-Many: Product → TransactionItems

## 🔒 Security Features

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

## 📈 Performance Optimization

### Database Optimization
- Room database dengan indexing
- Efficient queries dengan LiveData
- Background processing dengan AsyncTask
- Database migration support

### UI Performance
- RecyclerView dengan ViewHolder pattern
- Image caching dengan Glide
- Lazy loading untuk data besar
- Efficient memory management

## 🧪 Testing

### Unit Testing
- ViewModel testing
- Repository testing
- Utility class testing

### UI Testing
- Activity testing
- Fragment testing
- Navigation testing

## 📦 Build Variants

### Debug
- Development build
- Logging enabled
- Debug features

### Release
- Production build
- Optimized performance
- ProGuard enabled

## 🤝 Contributing

1. Fork repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

Untuk dukungan dan pertanyaan:
- Email: support@kasirkaskecil.com
- Documentation: [Wiki](https://github.com/username/kasir-kas-kecil/wiki)
- Issues: [GitHub Issues](https://github.com/username/kasir-kas-kecil/issues)

## 🔄 Changelog

### Version 1.0.0
- Initial release
- Basic POS functionality
- User authentication
- Product management
- Transaction processing
- Thermal printer support
- Basic reporting

---

**Kasir Kas Kecil** - Solusi POS modern untuk bisnis Anda! 🚀 