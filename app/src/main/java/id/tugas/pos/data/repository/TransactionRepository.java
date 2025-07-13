package id.tugas.pos.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;

import id.tugas.pos.data.database.PosDatabase;
import id.tugas.pos.data.database.TransactionDao;
import id.tugas.pos.data.database.TransactionItemDao;
import id.tugas.pos.data.model.Transaction;
import id.tugas.pos.data.model.TransactionItem;

public class TransactionRepository {
    
    private TransactionDao transactionDao;
    private TransactionItemDao transactionItemDao;
    private ExecutorService executorService;
    
    public TransactionRepository(Application application) {
        PosDatabase database = PosDatabase.getInstance(application);
        transactionDao = database.transactionDao();
        transactionItemDao = database.transactionItemDao();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    public long insertTransaction(Transaction transaction) {
        try {
            return executorService.submit(() -> transactionDao.insert(transaction)).get();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    public void insertTransactionItem(TransactionItem item) {
        executorService.execute(() -> {
            transactionItemDao.insert(item);
        });
    }
    
    public void updateTransaction(Transaction transaction) {
        executorService.execute(() -> {
            transactionDao.update(transaction);
        });
    }
    
    public void deleteTransaction(Transaction transaction) {
        executorService.execute(() -> {
            transactionDao.delete(transaction);
        });
    }
    
    public LiveData<List<Transaction>> getAllTransactions() {
        return transactionDao.getAllTransactions();
    }
    
    public LiveData<List<Transaction>> getRecentTransactions() {
        return transactionDao.getRecentTransactions();
    }
    
    public LiveData<List<Transaction>> getTransactionsByDate(String date) {
        return transactionDao.getTransactionsByDate(date);
    }
    
    public List<TransactionItem> getTransactionItemsSync(long transactionId) {
        return transactionItemDao.getItemsByTransactionId(transactionId);
    }
    
    public LiveData<Double> getTotalRevenue() {
        return transactionDao.getTotalRevenue();
    }
    
    public LiveData<Double> getTodayRevenue() {
        return transactionDao.getTodayRevenue();
    }
    
    public LiveData<Integer> getTodayTransactionCount() {
        return transactionDao.getTodayTransactionCount();
    }
    
    public LiveData<List<Transaction>> getTransactionsByDateRange(long startDate, long endDate) {
        return transactionDao.getTransactionsByDateRange(startDate, endDate);
    }
    
    public LiveData<Double> getRevenueByDateRange(long startDate, long endDate) {
        return transactionDao.getRevenueByDateRange(startDate, endDate);
    }
    
    // Additional methods for ViewModel compatibility
    public LiveData<Integer> getPendingTransactionCount() {
        return transactionDao.getPendingTransactionCount();
    }
    
    // Callback interfaces
    public interface OnTransactionOperationListener {
        void onSuccess();
        void onError(String message);
    }
    
    // Methods with callbacks
    public void addTransaction(Transaction transaction, OnTransactionOperationListener listener) {
        executorService.execute(() -> {
            try {
                transactionDao.insert(transaction);
                listener.onSuccess();
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        });
    }
    
    public void addTransactionItem(TransactionItem item, OnTransactionOperationListener listener) {
        executorService.execute(() -> {
            try {
                transactionItemDao.insert(item);
                listener.onSuccess();
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        });
    }
} 