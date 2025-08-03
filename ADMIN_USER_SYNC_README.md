# Solusi Masalah Koneksi Admin-User

## Masalah yang Ditemukan

Sistem POS saat ini memiliki masalah dimana data stok yang diinput oleh admin tidak muncul di device user, meskipun keduanya menggunakan toko yang sama. Hal ini disebabkan oleh:

1. **Database Lokal**: Setiap device memiliki database SQLite lokal yang terpisah
2. **Tidak Ada Sinkronisasi**: Tidak ada mekanisme sinkronisasi data antar device
3. **Store Selection**: Admin bisa memilih store, tapi data tidak terbagi ke user yang terkait

## Solusi yang Diimplementasikan

### 1. DatabaseSyncManager
- **File**: `app/src/main/java/id/tugas/pos/utils/DatabaseSyncManager.java`
- **Fungsi**: Mengelola sinkronisasi data antar device
- **Fitur**:
  - Sinkronisasi produk berdasarkan storeId
  - Update stok berdasarkan data stock in
  - Tracking timestamp sinkronisasi terakhir
  - Device ID untuk identifikasi device

### 2. SyncViewModel
- **File**: `app/src/main/java/id/tugas/pos/viewmodel/SyncViewModel.java`
- **Fungsi**: ViewModel untuk mengelola sinkronisasi
- **Fitur**:
  - Interface untuk sinkronisasi data
  - Status sinkronisasi real-time
  - Manajemen device ID

### 3. Tombol Sinkronisasi
- **File**: `app/src/main/res/layout/fragment_produk.xml`
- **Fungsi**: UI untuk memulai sinkronisasi
- **Fitur**:
  - Tombol "Sinkronisasi Data" di halaman produk
  - Status loading saat sinkronisasi
  - Feedback ke user

### 4. Enhanced ProductDao & StockInDao
- **File**: 
  - `app/src/main/java/id/tugas/pos/data/database/ProductDao.java`
  - `app/src/main/java/id/tugas/pos/data/database/StockInDao.java`
- **Fungsi**: Method tambahan untuk sinkronisasi
- **Fitur**:
  - `getAllActiveProductsByStoreSync()` - Get produk tanpa LiveData
  - `getTotalStockInForProduct()` - Hitung total stock in
  - `getAllStockInByStore()` - Get semua stock in per store

## Cara Kerja Sinkronisasi

### Untuk Admin:
1. Admin memilih toko dari dropdown
2. Admin input stok untuk produk
3. Admin klik tombol "Sinkronisasi Data"
4. Sistem menghitung ulang stok berdasarkan data stock in
5. Data tersimpan dengan storeId yang benar

### Untuk User:
1. User login dengan storeId yang sudah ditetapkan
2. User klik tombol "Sinkronisasi Data"
3. Sistem mengambil data produk untuk storeId user
4. Stok diupdate berdasarkan data stock in
5. User melihat data yang sudah disinkronkan

## Implementasi di ProdukFragment

```java
// Setup sync button
private void setupSyncButton() {
    if (btnSync != null) {
        btnSync.setOnClickListener(v -> {
            Integer currentStoreId = storeViewModel.getSelectedStoreId().getValue();
            if (currentStoreId != null) {
                syncViewModel.syncDataForStore(currentStoreId);
                Toast.makeText(requireContext(), "Memulai sinkronisasi data...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Pilih toko terlebih dahulu", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observe sync status
        syncViewModel.getIsSyncing().observe(getViewLifecycleOwner(), isSyncing -> {
            btnSync.setEnabled(!isSyncing);
            btnSync.setText(isSyncing ? "Sinkronisasi..." : "Sinkronisasi");
        });
    }
}
```

## Langkah Penggunaan

### 1. Setup Awal
1. Admin login dan pilih toko
2. Admin input produk dan stok
3. Admin klik "Sinkronisasi Data"

### 2. User Setup
1. User login dengan akun yang sudah ditetapkan storeId
2. User klik "Sinkronisasi Data"
3. User akan melihat data produk yang sama dengan admin

### 3. Maintenance
- Lakukan sinkronisasi secara berkala
- Pastikan storeId konsisten antara admin dan user
- Monitor log untuk debugging

## Troubleshooting

### Masalah: Data tidak sinkron
**Solusi**:
1. Pastikan admin dan user menggunakan storeId yang sama
2. Klik tombol "Sinkronisasi Data" di kedua device
3. Cek log untuk error

### Masalah: Stok tidak akurat
**Solusi**:
1. Pastikan data stock in sudah benar
2. Lakukan sinkronisasi ulang
3. Cek perhitungan stok di log

### Masalah: User tidak bisa akses data
**Solusi**:
1. Pastikan user memiliki storeId yang valid
2. Cek role user (ADMIN/USER)
3. Restart aplikasi jika perlu

## Catatan Penting

1. **Database Lokal**: Sistem masih menggunakan database lokal, jadi sinkronisasi hanya bekerja dalam satu device
2. **Manual Sync**: Sinkronisasi harus dilakukan manual dengan klik tombol
3. **StoreId**: Pastikan storeId konsisten antara admin dan user
4. **Backup**: Selalu backup data sebelum melakukan sinkronisasi besar

## Pengembangan Selanjutnya

Untuk sistem yang lebih robust, pertimbangkan:
1. **Cloud Database**: Migrasi ke Firebase atau server database
2. **Auto Sync**: Sinkronisasi otomatis saat ada perubahan
3. **Conflict Resolution**: Penanganan konflik data
4. **Offline Support**: Sinkronisasi saat online
5. **Push Notifications**: Notifikasi saat ada update data 