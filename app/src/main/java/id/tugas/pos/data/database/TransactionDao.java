package id.tugas.pos.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import id.tugas.pos.data.model.Transaction;

@Dao
public interface TransactionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Transaction transaction);
    
    @Update
    void update(Transaction transaction);
    
    @Delete
    void delete(Transaction transaction);
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    LiveData<Transaction> getTransactionById(int id);
    
    @Query("SELECT * FROM transactions WHERE transactionNumber = :transactionNumber")
    LiveData<Transaction> getTransactionByNumber(String transactionNumber);
    
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    LiveData<List<Transaction>> getAllTransactions();
    
    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY createdAt DESC")
    LiveData<List<Transaction>> getTransactionsByStatus(String status);
    
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<Transaction>> getTransactionsByUser(int userId);
    
    @Query("SELECT * FROM transactions WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    LiveData<List<Transaction>> getTransactionsByDateRange(long startDate, long endDate);
    
    @Query("SELECT * FROM transactions WHERE paymentMethod = :paymentMethod ORDER BY createdAt DESC")
    LiveData<List<Transaction>> getTransactionsByPaymentMethod(String paymentMethod);
    
    @Query("SELECT COUNT(*) FROM transactions WHERE status = 'COMPLETED'")
    LiveData<Integer> getCompletedTransactionCount();
    
    @Query("SELECT SUM(total) FROM transactions WHERE status = 'COMPLETED'")
    LiveData<Double> getTotalRevenue();
    
    @Query("SELECT SUM(total) FROM transactions WHERE status = 'COMPLETED' AND createdAt BETWEEN :startDate AND :endDate")
    LiveData<Double> getRevenueByDateRange(long startDate, long endDate);
    
    @Query("SELECT SUM(total) FROM transactions WHERE status = 'COMPLETED' AND userId = :userId")
    LiveData<Double> getRevenueByUser(int userId);
    
    @Query("SELECT * FROM transactions WHERE status = 'PENDING' ORDER BY createdAt ASC")
    LiveData<List<Transaction>> getPendingTransactions();
    
    @Query("SELECT COUNT(*) FROM transactions WHERE status = 'PENDING'")
    LiveData<Integer> getPendingTransactionCount();
    
    @Query("UPDATE transactions SET status = :status WHERE id = :transactionId")
    void updateTransactionStatus(int transactionId, String status);
    
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC LIMIT :limit")
    LiveData<List<Transaction>> getRecentTransactions(int limit);
    
    // Additional methods for repository compatibility
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC LIMIT 50")
    LiveData<List<Transaction>> getRecentTransactions();
    
    @Query("SELECT * FROM transactions WHERE DATE(createdAt/1000, 'unixepoch') = :date ORDER BY createdAt DESC")
    LiveData<List<Transaction>> getTransactionsByDate(String date);
    
    @Query("SELECT SUM(total) FROM transactions WHERE status = 'COMPLETED' AND DATE(createdAt/1000, 'unixepoch') = DATE('now')")
    LiveData<Double> getTodayRevenue();
    
    @Query("SELECT COUNT(*) FROM transactions WHERE status = 'COMPLETED' AND DATE(createdAt/1000, 'unixepoch') = DATE('now')")
    LiveData<Integer> getTodayTransactionCount();
} 