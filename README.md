# MyPOS - Android Point of Sale Application

Aplikasi Point of Sale (POS) Android yang dirancang untuk usaha kecil dan menengah dengan fitur lengkap untuk manajemen transaksi, produk, laporan bisnis, dan sistem multi-toko.

## 🚀 Fitur Utama

### 🔐 Sistem Autentikasi
- **Role-based Authentication**: Admin, Manager, dan User/Kasir
- **Multi-Store Support**: Manajemen multiple toko
- **Login System**: Email dan password
- **Session Management**: Auto-logout dan session tracking

### 📊 Dashboard
- **Real-time Metrics**: Pendapatan, penjualan hari ini, total produk
- **Stock Monitoring**: Alert stok menipis dan habis
- **Quick Actions**: Akses cepat ke fitur utama
- **Profit Analysis**: Analisis margin keuntungan
- **Modal Awal**: Tracking modal awal harian per toko

### 🛍️ Manajemen Produk
- **Product Management**: CRUD operasi untuk produk
- **Category Management**: Pengelolaan kategori produk
- **Stock Management**: Monitoring stok real-time dengan stock in/out
- **Barcode Support**: Scanner barcode untuk produk
- **Image Support**: Upload dan manajemen gambar produk
- **Multi-Store Inventory**: Stok terpisah per toko

### 💰 Transaksi POS
- **Sales Transaction**: Proses penjualan dengan interface yang mudah
- **Payment Methods**: Tunai, kartu, transfer
- **Receipt Printing**: Cetak struk menggunakan printer thermal
- **Discount & Tax**: Perhitungan diskon dan pajak
- **Change Calculation**: Perhitungan kembalian otomatis
- **Multi-Store Transactions**: Transaksi terpisah per toko

### 📋 Riwayat Transaksi
- **Transaction History**: Riwayat lengkap semua transaksi
- **Search & Filter**: Pencarian dan filter berdasarkan tanggal, status, kasir, toko
- **Transaction Details**: Detail lengkap setiap transaksi
- **Receipt Reprint**: Cetak ulang struk

### 💸 Manajemen Pengeluaran
- **Expense Tracking**: Pencatatan pengeluaran bisnis
- **Category Management**: Kategori pengeluaran (Operasional, Utilitas, Gaji, dll)
- **Receipt Attachment**: Lampiran bukti pengeluaran
- **Expense Reports**: Laporan pengeluaran per toko

### 📈 Laporan dan Analisis
- **Sales Reports**: Laporan penjualan harian, mingguan, bulanan
- **Profit Reports**: Analisis keuntungan dan margin
- **Inventory Reports**: Laporan stok dan inventori
- **Stock Movement Reports**: Laporan keluar masuk stok
- **Export Features**: Ekspor ke PDF dan Excel
- **Chart Visualization**: Grafik dan visualisasi data
- **Multi-Store Reports**: Laporan terpisah atau gabungan per toko

### 🏪 Manajemen Multi-Toko
- **Store Management**: CRUD operasi untuk toko
- **Store Selection**: Pemilihan toko aktif untuk admin
- **Store-specific Data**: Data terpisah per toko
- **Cross-Store Analytics**: Analisis lintas toko untuk admin

### ⚙️ Pengaturan
- **Printer Configuration**: Pengaturan printer thermal
- **Company Information**: Informasi perusahaan
- **Tax Settings**: Konfigurasi pajak
- **User Management**: Manajemen user dan role
- **Store Assignment**: Assignment user ke toko tertentu

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
│   ├── model/              # Entity classes
│   │   ├── User.java       # User entity dengan role
│   │   ├── Store.java      # Store entity untuk multi-toko
│   │   ├── Product.java    # Product entity
│   │   ├── Category.java   # Product category entity
│   │   ├── Transaction.java # Transaction entity
│   │   ├── TransactionItem.java # Transaction items
│   │   ├── Expense.java    # Expense entity
│   │   ├── StockIn.java    # Stock in movement
│   │   └── ModalAwal.java  # Daily starting capital
│   ├── database/           # Room Database & DAOs
│   │   ├── PosDatabase.java
│   │   ├── UserDao.java
│   │   ├── StoreDao.java
│   │   ├── ProductDao.java
│   │   ├── CategoryDao.java
│   │   ├── TransactionDao.java
│   │   ├── TransactionItemDao.java
│   │   ├── ExpenseDao.java
│   │   ├── StockInDao.java
│   │   └── ModalAwalDao.java
│   └── repository/         # Repository classes
├── ui/
│   ├── login/             # Login screen
│   ├── dashboard/         # Dashboard screen
│   ├── produk/            # Product management
│   ├── transaksi/         # POS transaction
│   ├── history/           # Transaction history
│   ├── expense/           # Expense management
│   ├── report/            # Reports & analytics
│   ├── settings/          # App settings
│   ├── user/              # User management
│   └── stockin/           # Stock in management
├── viewmodel/             # ViewModel classes
├── utils/                 # Utility classes
│   ├── CurrencyUtils.java # Currency formatting
│   ├── ExcelExporter.java # Excel export functionality
│   └── PermissionHelper.java # Permission handling
└── MainActivity.java     # Main activity with navigation
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
- **Excel Export**: Apache POI
- **Date/Time**: ThreeTenABP

### Libraries
- **Room**: Local database dengan migration support
- **LiveData**: Reactive programming
- **ViewModel**: Lifecycle-aware components
- **Material Design**: UI components
- **ESCPOS-ThermalPrinter**: Thermal printer support
- **WorkManager**: Background tasks
- **Apache POI**: Excel file generation
- **Gson**: JSON serialization

## 📊 Database Schema

### Entity Relationship Diagram
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│    Store    │     │    User     │     │  Category   │
│             │     │             │     │             │
│ - id (PK)   │◄────│ - id (PK)   │     │ - id (PK)   │
│ - name      │     │ - email     │     │ - name      │
│ - address   │     │ - password  │     │ - createdAt │
│ - phone     │     │ - role      │     └─────────────┘
│ - createdAt │     │ - storeId   │            │
└─────────────┘     │ - createdAt │            │
       │            └─────────────┘            │
       │                   │                  │
       │                   │                  ▼
       │            ┌─────────────┐     ┌─────────────┐
       │            │ Transaction │     │   Product   │
       │            │             │     │             │
       │            │ - id (PK)   │     │ - id (PK)   │
       │            │ - userId    │     │ - name      │
       │            │ - storeId   │     │ - categoryId│
       │            │ - total     │     │ - price     │
       │            │ - status    │     │ - stock     │
       │            │ - createdAt │     │ - storeId   │
       │            └─────────────┘     │ - createdAt │
       │                   │            └─────────────┘
       │                   │                   │
       │                   ▼                   │
       │            ┌─────────────┐            │
       │            │TransactionItem           │
       │            │             │            │
       │            │ - id (PK)   │            │
       │            │ - transactionId ─────────┘
       │            │ - productId │◄───────────┘
       │            │ - quantity  │
       │            │ - price     │
       │            │ - subtotal  │
       │            └─────────────┘
       │
       ├─────────────┐
       │             ▼
       │      ┌─────────────┐
       │      │   Expense   │
       │      │             │
       │      │ - id (PK)   │
       │      │ - title     │
       │      │ - amount    │
       │      │ - category  │
       │      │ - storeId   │
       │      │ - createdAt │
       │      └─────────────┘
       │
       ├─────────────┐
       │             ▼
       │      ┌─────────────┐
       │      │  ModalAwal  │
       │      │             │
       │      │ - id (PK)   │
       │      │ - tanggal   │
       │      │ - nominal   │
       │      │ - storeId   │
       │      │ - createdAt │
       │      └─────────────┘
       │
       └─────────────┐
                     ▼
              ┌─────────────┐
              │   StockIn   │
              │             │
              │ - id (PK)   │
              │ - productId │
              │ - quantity  │
              │ - storeId   │
              │ - createdAt │
              └─────────────┘
```

### Database Tables Detail

#### **Users Table**
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    fullName TEXT,
    role TEXT NOT NULL DEFAULT 'USER', -- ADMIN, MANAGER, USER
    storeId INTEGER,
    isActive BOOLEAN DEFAULT 1,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    FOREIGN KEY (storeId) REFERENCES stores(id)
);
```

#### **Stores Table**
```sql
CREATE TABLE stores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    address TEXT,
    phone TEXT,
    isActive BOOLEAN DEFAULT 1,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);
```

#### **Categories Table**
```sql
CREATE TABLE categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    createdAt INTEGER NOT NULL
);
```

#### **Products Table**
```sql
CREATE TABLE products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    price REAL NOT NULL,
    stock INTEGER DEFAULT 0,
    category TEXT,
    categoryId INTEGER,
    imagePath TEXT,
    storeId INTEGER NOT NULL,
    isActive BOOLEAN DEFAULT 1,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    FOREIGN KEY (storeId) REFERENCES stores(id),
    FOREIGN KEY (categoryId) REFERENCES categories(id)
);
```

#### **Transactions Table**
```sql
CREATE TABLE transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transactionNumber TEXT UNIQUE,
    userId INTEGER NOT NULL,
    storeId INTEGER NOT NULL,
    customerName TEXT,
    subtotal REAL NOT NULL,
    tax REAL DEFAULT 0,
    discount REAL DEFAULT 0,
    total REAL NOT NULL,
    paymentMethod TEXT, -- CASH, CARD, TRANSFER
    cashReceived REAL DEFAULT 0,
    change REAL DEFAULT 0,
    status TEXT DEFAULT 'PENDING', -- PENDING, COMPLETED, CANCELLED
    notes TEXT,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    FOREIGN KEY (userId) REFERENCES users(id),
    FOREIGN KEY (storeId) REFERENCES stores(id)
);
```

#### **Transaction Items Table**
```sql
CREATE TABLE transaction_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transactionId INTEGER NOT NULL,
    productId INTEGER NOT NULL,
    productName TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    price REAL NOT NULL,
    subtotal REAL NOT NULL,
    FOREIGN KEY (transactionId) REFERENCES transactions(id) ON DELETE CASCADE,
    FOREIGN KEY (productId) REFERENCES products(id)
);
```

#### **Expenses Table**
```sql
CREATE TABLE expenses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    amount REAL NOT NULL,
    category TEXT, -- OPERATIONAL, UTILITIES, SALARY, OTHER
    paymentMethod TEXT,
    userId INTEGER NOT NULL,
    storeId INTEGER NOT NULL,
    receiptPath TEXT,
    expenseDate INTEGER NOT NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    FOREIGN KEY (userId) REFERENCES users(id),
    FOREIGN KEY (storeId) REFERENCES stores(id)
);
```

#### **Stock In Table**
```sql
CREATE TABLE stock_in (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    productId INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    storeId INTEGER NOT NULL,
    note TEXT,
    createdAt INTEGER NOT NULL,
    FOREIGN KEY (productId) REFERENCES products(id),
    FOREIGN KEY (storeId) REFERENCES stores(id)
);
```

#### **Modal Awal Table**
```sql
CREATE TABLE modal_awal (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tanggal INTEGER NOT NULL, -- Format: YYYYMMDD
    nominal REAL NOT NULL,
    storeId INTEGER NOT NULL,
    createdAt INTEGER NOT NULL,
    FOREIGN KEY (storeId) REFERENCES stores(id),
    UNIQUE(tanggal, storeId)
);
```

### Database Relationships
- **One-to-Many**: Store → Users, Products, Transactions, Expenses, StockIn, ModalAwal
- **One-to-Many**: User → Transactions, Expenses
- **One-to-Many**: Transaction → TransactionItems
- **One-to-Many**: Product → TransactionItems, StockIn
- **One-to-Many**: Category → Products

### Indexes untuk Performance
```sql
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_store ON users(storeId);
CREATE INDEX idx_products_store ON products(storeId);
CREATE INDEX idx_products_category ON products(categoryId);
CREATE INDEX idx_transactions_store ON transactions(storeId);
CREATE INDEX idx_transactions_user ON transactions(userId);
CREATE INDEX idx_transactions_date ON transactions(createdAt);
CREATE INDEX idx_transaction_items_transaction ON transaction_items(transactionId);
CREATE INDEX idx_expenses_store ON expenses(storeId);
CREATE INDEX idx_stock_in_product ON stock_in(productId);
CREATE INDEX idx_modal_awal_date_store ON modal_awal(tanggal, storeId);
```

## 🔧 Setup dan Instalasi

### Prerequisites
- Android Studio Hedgehog atau lebih baru
- Android SDK API 26+ (Android 8.0)
- Java 8 atau lebih baru
- Gradle 8.0+

### Installation Steps
1. Clone repository ini
2. Buka project di Android Studio
3. Setup keystore di `local.properties`:
   ```properties
   KEYSTORE_FILE=path/to/keystore.jks
   KEYSTORE_PASSWORD=your_password
   KEY_ALIAS=your_alias
   KEY_PASSWORD=your_key_password
   ```
4. Sync Gradle files
5. Build project
6. Run di device atau emulator

### Default Credentials
```
Admin:
- Username: aidilfitriyoka2812@gmail.com
- Password: admin123
```

## 📱 UI/UX Features

### Responsive Design
- **Phone Layout**: Optimized untuk mobile devices
- **Tablet Layout**: Enhanced layout untuk tablets
- **Landscape Support**: Responsive landscape orientation
- **Material Design**: Modern Material Design 3 components

### Navigation
- **Navigation Drawer**: Side navigation menu
- **Fragment-based**: Modular screen management
- **Store Selection**: Dynamic store switching untuk admin
- **Role-based UI**: Interface menyesuaikan role user

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

## 📈 Export Features

### PDF Export
- Laporan transaksi
- Laporan pengeluaran
- Laporan stok
- Custom date range

### Excel Export
- Export menggunakan Apache POI
- Format .xlsx dengan styling
- Currency formatting
- Auto-sized columns
- Timestamp dalam filename

## 🔒 Security Features

### Authentication
- Password-based authentication
- Role-based access control (ADMIN, MANAGER, USER)
- Session management
- Store-based data isolation

### Data Protection
- Local database dengan Room
- Input validation
- SQL injection prevention
- Secure keystore configuration

## 📦 Build Configuration

### Signing Configuration
- Keystore dari `local.properties`
- Sama untuk debug dan release builds
- Automated APK naming dengan timestamp

### Build Variants
- **Debug**: Development dengan logging
- **Release**: Production optimized

### APK Naming
Format: `MyPos-{BuildType}-v{VersionName}-{VersionCode}-{Timestamp}.apk`

## 🤝 Contributing

1. Fork repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔄 Changelog

### Version 1.0.0
- Initial release
- Multi-store support
- User role management
- Product management dengan kategori
- POS transaction system
- Stock in/out tracking
- Modal awal tracking
- Excel export functionality
- Thermal printer support
- Comprehensive reporting

---

**MyPOS** - Solusi POS modern untuk bisnis multi-toko Anda! 🚀
