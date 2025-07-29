package id.tugas.pos.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;

import id.tugas.pos.data.database.PosDatabase;
import id.tugas.pos.data.database.TransactionDao;
import id.tugas.pos.data.database.TransactionItemDao;
import id.tugas.pos.data.database.ProductDao;
import id.tugas.pos.data.model.Transaction;
import id.tugas.pos.data.model.TransactionItem;

public class TransactionRepository {
    
    private TransactionDao transactionDao;
    private TransactionItemDao transactionItemDao;
    private ProductDao productDao;
    private ExecutorService executorService;
    
    public TransactionRepository(Application application) {
        PosDatabase database = PosDatabase.getInstance(application);
        transactionDao = database.transactionDao();
        transactionItemDao = database.transactionItemDao();
        productDao = database.productDao();
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
            productDao.decreaseStock(item.getProductId(), item.getQuantity());
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
    
    public LiveData<Double> getRevenueByDateRangeAndStore(long startDate, long endDate, Integer storeId) {
        if (storeId == null) {
            return transactionDao.getRevenueByDateRange(startDate, endDate);
        }
        return transactionDao.getRevenueByDateRangeAndStore(startDate, endDate, storeId);
    }
    
    public LiveData<List<Transaction>> getAllTransactionsByStore(int storeId) {
        return transactionDao.getAllTransactionsByStore(storeId);
    }
    
    public LiveData<Double> getTotalRevenueByStore(Integer storeId) {
        if (storeId == null) {
            return transactionDao.getTotalRevenue();
        }
        return transactionDao.getTotalRevenueByStore(storeId);
    }
    
    public LiveData<Integer> getPendingTransactionCountByStore(Integer storeId) {
        if (storeId == null) {
            return transactionDao.getPendingTransactionCount();
        }
        return transactionDao.getPendingTransactionCountByStore(storeId);
    }
    
    // Additional methods for ViewModel compatibility
    public LiveData<Integer> getPendingTransactionCount() {
        return transactionDao.getPendingTransactionCount();
    }
    
    // Laporan transaksi dengan filter tanggal
    public LiveData<List<id.tugas.pos.ui.report.LaporanTransaksiItem>> getLaporanTransaksi(long startDate, long endDate) {
        MutableLiveData<List<id.tugas.pos.ui.report.LaporanTransaksiItem>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<id.tugas.pos.ui.report.LaporanTransaksiItem> data = transactionItemDao.getLaporanTransaksi(startDate, endDate);
            liveData.postValue(data);
        });
        return liveData;
    }
    
    // Laporan transaksi dengan filter tanggal dan store
    public LiveData<List<id.tugas.pos.ui.report.LaporanTransaksiItem>> getLaporanTransaksiByStore(long startDate, long endDate, int storeId) {
        MutableLiveData<List<id.tugas.pos.ui.report.LaporanTransaksiItem>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<id.tugas.pos.ui.report.LaporanTransaksiItem> data = transactionItemDao.getLaporanTransaksiByStore(startDate, endDate, storeId);
            liveData.postValue(data);
        });
        return liveData;
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
    
    public void updateTransaction(Transaction transaction, OnTransactionOperationListener listener) {
        executorService.execute(() -> {
            try {
                transactionDao.update(transaction);
                listener.onSuccess();
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        });
    }
} 