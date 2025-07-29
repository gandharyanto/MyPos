package id.tugas.pos.data.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import id.tugas.pos.ui.report.LaporanPengeluaranItem;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.List;

import id.tugas.pos.data.database.PosDatabase;
import id.tugas.pos.data.database.ExpenseDao;
import id.tugas.pos.data.model.Expense;

public class ExpenseRepository {
    
    private ExpenseDao expenseDao;
    private LiveData<List<Expense>> allExpenses;
    private LiveData<Double> totalExpenses;
    private LiveData<Integer> expenseCount;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    public ExpenseRepository(Application application) {
        PosDatabase database = PosDatabase.getInstance(application);
        expenseDao = database.expenseDao();
        allExpenses = expenseDao.getAllExpenses();
        totalExpenses = expenseDao.getTotalExpenses();
        expenseCount = expenseDao.getExpenseCount();
    }
    
    // Insert expense
    public void insert(Expense expense) {
        new InsertExpenseAsyncTask(expenseDao).execute(expense);
    }
    
    // Update expense
    public void update(Expense expense) {
        new UpdateExpenseAsyncTask(expenseDao).execute(expense);
    }
    
    // Delete expense
    public void delete(Expense expense) {
        new DeleteExpenseAsyncTask(expenseDao).execute(expense);
    }
    
    // Get expense by ID
    public LiveData<Expense> getExpenseById(int id) {
        return expenseDao.getExpenseById(id);
    }
    
    // Get all expenses
    public LiveData<List<Expense>> getAllExpenses() {
        return allExpenses;
    }
    
    // Get expenses by category
    public LiveData<List<Expense>> getExpensesByCategory(String category) {
        return expenseDao.getExpensesByCategory(category);
    }
    
    // Get expenses by user
    public LiveData<List<Expense>> getExpensesByUser(int userId) {
        return expenseDao.getExpensesByUser(userId);
    }
    
    // Get expenses by date range
    public LiveData<List<Expense>> getExpensesByDateRange(long startDate, long endDate) {
        return expenseDao.getExpensesByDateRange(startDate, endDate);
    }
    
    // Get expenses by payment method
    public LiveData<List<Expense>> getExpensesByPaymentMethod(String paymentMethod) {
        return expenseDao.getExpensesByPaymentMethod(paymentMethod);
    }
    
    // Get total expenses
    public LiveData<Double> getTotalExpenses() {
        return totalExpenses;
    }
    
    // Get total expenses by category
    public LiveData<Double> getTotalExpensesByCategory(String category) {
        return expenseDao.getTotalExpensesByCategory(category);
    }
    
    // Get total expenses by date range
    public LiveData<Double> getTotalExpensesByDateRange(long startDate, long endDate) {
        return expenseDao.getTotalExpensesByDateRange(startDate, endDate);
    }
    
    // Get total expenses by user
    public LiveData<Double> getTotalExpensesByUser(int userId) {
        return expenseDao.getTotalExpensesByUser(userId);
    }
    
    // Get all expense categories
    public LiveData<List<String>> getAllExpenseCategories() {
        return expenseDao.getAllExpenseCategories();
    }
    
    // Get expense count
    public LiveData<Integer> getExpenseCount() {
        return expenseCount;
    }
    
    // Get recent expenses
    public LiveData<List<Expense>> getRecentExpenses(int limit) {
        return expenseDao.getRecentExpenses(limit);
    }
    
    // Get expenses above amount
    public LiveData<List<Expense>> getExpensesAboveAmount(double minAmount) {
        return expenseDao.getExpensesAboveAmount(minAmount);
    }

    public LiveData<List<Expense>> getAllExpensesByStore(int storeId) {
        return expenseDao.getAllExpensesByStore(storeId);
    }

    public LiveData<Double> getTotalExpensesByStore(int storeId) {
        return expenseDao.getTotalExpensesByStore(storeId);
    }
    
    public LiveData<Double> getTotalExpensesByStore(Integer storeId) {
        if (storeId == null) {
            return totalExpenses;
        }
        return expenseDao.getTotalExpensesByStore(storeId);
    }

    public LiveData<Integer> getExpenseCountByStore(int storeId) {
        return expenseDao.getExpenseCountByStore(storeId);
    }
    
    // Laporan pengeluaran dengan filter tanggal
    public LiveData<List<LaporanPengeluaranItem>> getLaporanPengeluaran(long startDate, long endDate) {
        MutableLiveData<List<LaporanPengeluaranItem>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<LaporanPengeluaranItem> data = expenseDao.getLaporanPengeluaran(startDate, endDate);
            liveData.postValue(data);
        });
        return liveData;
    }
    
    // Laporan pengeluaran dengan filter tanggal dan store
    public LiveData<List<LaporanPengeluaranItem>> getLaporanPengeluaranByStore(long startDate, long endDate, int storeId) {
        MutableLiveData<List<LaporanPengeluaranItem>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<LaporanPengeluaranItem> data = expenseDao.getLaporanPengeluaranByStore(startDate, endDate, storeId);
            liveData.postValue(data);
        });
        return liveData;
    }
    
    // AsyncTask classes
    private static class InsertExpenseAsyncTask extends AsyncTask<Expense, Void, Void> {
        private ExpenseDao expenseDao;
        
        InsertExpenseAsyncTask(ExpenseDao expenseDao) {
            this.expenseDao = expenseDao;
        }
        
        @Override
        protected Void doInBackground(Expense... expenses) {
            expenseDao.insert(expenses[0]);
            return null;
        }
    }
    
    private static class UpdateExpenseAsyncTask extends AsyncTask<Expense, Void, Void> {
        private ExpenseDao expenseDao;
        
        UpdateExpenseAsyncTask(ExpenseDao expenseDao) {
            this.expenseDao = expenseDao;
        }
        
        @Override
        protected Void doInBackground(Expense... expenses) {
            expenseDao.update(expenses[0]);
            return null;
        }
    }
    
    private static class DeleteExpenseAsyncTask extends AsyncTask<Expense, Void, Void> {
        private ExpenseDao expenseDao;
        
        DeleteExpenseAsyncTask(ExpenseDao expenseDao) {
            this.expenseDao = expenseDao;
        }
        
        @Override
        protected Void doInBackground(Expense... expenses) {
            expenseDao.delete(expenses[0]);
            return null;
        }
    }
} 