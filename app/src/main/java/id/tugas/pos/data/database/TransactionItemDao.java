package id.tugas.pos.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import id.tugas.pos.data.model.TransactionItem;
import id.tugas.pos.ui.report.LaporanStokItem;
import id.tugas.pos.ui.report.LaporanTransaksiItem;

@Dao
public interface TransactionItemDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TransactionItem transactionItem);
    
    @Update
    void update(TransactionItem transactionItem);
    
    @Delete
    void delete(TransactionItem transactionItem);
    
    @Query("SELECT * FROM transaction_items WHERE id = :id")
    LiveData<TransactionItem> getTransactionItemById(int id);
    
    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    LiveData<List<TransactionItem>> getTransactionItemsByTransactionId(int transactionId);
    
    @Query("SELECT * FROM transaction_items WHERE productId = :productId")
    LiveData<List<TransactionItem>> getTransactionItemsByProductId(int productId);
    
    @Query("SELECT COUNT(*) FROM transaction_items WHERE transactionId = :transactionId")
    LiveData<Integer> getTransactionItemCount(int transactionId);
    
    @Query("SELECT SUM(total) FROM transaction_items WHERE transactionId = :transactionId")
    LiveData<Double> getTransactionTotal(int transactionId);
    
    @Query("SELECT SUM(quantity) FROM transaction_items WHERE productId = :productId")
    LiveData<Integer> getTotalQuantitySold(int productId);
    
    @Query("SELECT SUM(total) FROM transaction_items WHERE productId = :productId")
    LiveData<Double> getTotalRevenueByProduct(int productId);
    
    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    List<TransactionItem> getTransactionItemsByTransactionIdSync(int transactionId);
    
    // Additional method for repository compatibility
    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    List<TransactionItem> getItemsByTransactionId(long transactionId);
    
    @Query("SELECT * FROM transaction_items")
    List<TransactionItem> getAllTransactionItemsSync();
    
    @Query("DELETE FROM transaction_items WHERE transactionId = :transactionId")
    void deleteTransactionItemsByTransactionId(int transactionId);
    
    // Method to calculate total quantity sold for a specific product
    @Query("SELECT COALESCE(SUM(quantity), 0) FROM transaction_items WHERE productId = :productId")
    int getTotalQuantitySoldForProduct(int productId);
    
    @Query("SELECT productId, SUM(quantity) as totalQuantity FROM transaction_items GROUP BY productId ORDER BY totalQuantity DESC LIMIT :limit")
    LiveData<List<ProductSalesSummary>> getTopSellingProducts(int limit);
    
    @Query("SELECT productName as namaProduk, SUM(quantity) as jumlahTerjual, SUM(total) as totalHarga " +
           "FROM transaction_items " +
           "WHERE createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY productName")
    List<LaporanTransaksiItem> getLaporanTransaksi(long startDate, long endDate);
    
    @Query("SELECT productName as namaProduk, SUM(quantity) as jumlahTerjual, SUM(total) as totalHarga " +
           "FROM transaction_items " +
           "GROUP BY productName")
    List<LaporanTransaksiItem> getLaporanTransaksiAll();
    
    @Query("SELECT ti.productName as namaProduk, SUM(ti.quantity) as jumlahTerjual, SUM(ti.total) as totalHarga " +
           "FROM transaction_items ti " +
           "INNER JOIN transactions t ON ti.transactionId = t.id " +
           "WHERE ti.createdAt BETWEEN :startDate AND :endDate " +
           "AND t.storeId = :storeId " +
           "GROUP BY ti.productName")
    List<LaporanTransaksiItem> getLaporanTransaksiByStore(long startDate, long endDate, int storeId);
    
    @Query("SELECT productName as namaProduk, 0 as stokMasuk, SUM(quantity) as stokKeluar, 0 as stokTersisa " +
           "FROM transaction_items WHERE createdAt BETWEEN :startDate AND :endDate GROUP BY productId")
    List<LaporanStokItem> getLaporanStokKeluar(long startDate, long endDate);
    
    @Query("SELECT ti.productName as namaProduk, 0 as stokMasuk, SUM(ti.quantity) as stokKeluar, 0 as stokTersisa " +
           "FROM transaction_items ti " +
           "INNER JOIN transactions t ON ti.transactionId = t.id " +
           "WHERE ti.createdAt BETWEEN :startDate AND :endDate " +
           "AND t.storeId = :storeId " +
           "GROUP BY ti.productId")
    List<LaporanStokItem> getLaporanStokKeluarByStore(long startDate, long endDate, int storeId);
    
    public static class ProductSalesSummary {
        public int productId;
        public int totalQuantity;
    }
} 