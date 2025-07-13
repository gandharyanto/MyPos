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
    
    @Query("DELETE FROM transaction_items WHERE transactionId = :transactionId")
    void deleteTransactionItemsByTransactionId(int transactionId);
    
    @Query("SELECT productId, SUM(quantity) as totalQuantity FROM transaction_items GROUP BY productId ORDER BY totalQuantity DESC LIMIT :limit")
    LiveData<List<ProductSalesSummary>> getTopSellingProducts(int limit);
    
    public static class ProductSalesSummary {
        public int productId;
        public int totalQuantity;
    }
} 