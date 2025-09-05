package id.tugas.pos.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import id.tugas.pos.data.model.Expense;

@Dao
public interface ExpenseDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Expense expense);
    
    @Update
    void update(Expense expense);
    
    @Delete
    void delete(Expense expense);
    
    @Query("SELECT * FROM expenses WHERE id = :id")
    LiveData<Expense> getExpenseById(int id);
    
    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC")
    LiveData<List<Expense>> getAllExpenses();
    
    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY expenseDate DESC")
    LiveData<List<Expense>> getExpensesByCategory(String category);
    
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY expenseDate DESC")
    LiveData<List<Expense>> getExpensesByUser(int userId);
    
    @Query("SELECT * FROM expenses WHERE expenseDate BETWEEN :startDate AND :endDate ORDER BY expenseDate DESC")
    LiveData<List<Expense>> getExpensesByDateRange(long startDate, long endDate);
    
    @Query("SELECT * FROM expenses WHERE paymentMethod = :paymentMethod ORDER BY expenseDate DESC")
    LiveData<List<Expense>> getExpensesByPaymentMethod(String paymentMethod);
    
    @Query("SELECT SUM(amount) FROM expenses")
    LiveData<Double> getTotalExpenses();
    
    @Query("SELECT SUM(amount) FROM expenses WHERE category = :category")
    LiveData<Double> getTotalExpensesByCategory(String category);
    
    @Query("SELECT SUM(amount) FROM expenses WHERE expenseDate BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalExpensesByDateRange(long startDate, long endDate);
    
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId")
    LiveData<Double> getTotalExpensesByUser(int userId);
    
    @Query("SELECT DISTINCT category FROM expenses ORDER BY category ASC")
    LiveData<List<String>> getAllExpenseCategories();
    
    @Query("SELECT COUNT(*) FROM expenses")
    LiveData<Integer> getExpenseCount();
    
    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC LIMIT :limit")
    LiveData<List<Expense>> getRecentExpenses(int limit);
    
    @Query("SELECT * FROM expenses WHERE amount >= :minAmount ORDER BY amount DESC")
    LiveData<List<Expense>> getExpensesAboveAmount(double minAmount);
    
    @Query("SELECT * FROM expenses WHERE storeId = :storeId ORDER BY expenseDate DESC")
    LiveData<List<Expense>> getAllExpensesByStore(int storeId);
    
    @Query("SELECT SUM(amount) FROM expenses WHERE storeId = :storeId")
    LiveData<Double> getTotalExpensesByStore(int storeId);

    @Query("SELECT COUNT(*) FROM expenses WHERE storeId = :storeId")
    LiveData<Integer> getExpenseCountByStore(int storeId);

    @Query("SELECT expenseDate as tanggal, category as kategori, amount as nominal, description as keterangan " +
           "FROM expenses " +
           "WHERE expenseDate BETWEEN :startDate AND :endDate " +
           "ORDER BY expenseDate ASC")
    List<id.tugas.pos.ui.report.LaporanPengeluaranItem> getLaporanPengeluaran(long startDate, long endDate);
    
    @Query("SELECT expenseDate as tanggal, category as kategori, amount as nominal, description as keterangan " +
           "FROM expenses " +
           "WHERE expenseDate BETWEEN :startDate AND :endDate " +
           "AND storeId = :storeId " +
           "ORDER BY expenseDate ASC")
    List<id.tugas.pos.ui.report.LaporanPengeluaranItem> getLaporanPengeluaranByStore(long startDate, long endDate, int storeId);

    @Query("SELECT * FROM expenses WHERE expenseDate BETWEEN :startDate AND :endDate AND storeId = :storeId ORDER BY expenseDate DESC")
    LiveData<List<Expense>> getExpensesByDateRangeAndStore(long startDate, long endDate, int storeId);
}
