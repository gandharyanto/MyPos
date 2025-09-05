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
import id.tugas.pos.ui.report.LaporanTransaksiItem;

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
            try {
                System.out.println("DEBUG: Processing transaction item - Product ID: " + item.getProductId() +
                        ", Quantity: " + item.getQuantity() + ", Price: " + item.getPrice());

                // First insert the transaction item
                transactionItemDao.insert(item);
                System.out.println("DEBUG: Transaction item inserted successfully");

                // Then decrease the stock atomically
                productDao.decreaseStock(item.getProductId(), item.getQuantity());
                System.out.println("DEBUG: Stock decreased for product ID: " + item.getProductId() +
                        " by quantity: " + item.getQuantity());

            } catch (Exception e) {
                e.printStackTrace();
                // Log error for debugging
                System.err.println("Error inserting transaction item: " + e.getMessage());
            }
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

    public LiveData<List<Transaction>> getTransactionsByDateRange(long startDate, long endDate) {
        return transactionDao.getTransactionsByDateRange(startDate, endDate);
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
    public LiveData<List<LaporanTransaksiItem>> getLaporanTransaksi(long startDate, long endDate) {
        MutableLiveData<List<LaporanTransaksiItem>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            try {
                System.out.println("DEBUG: Loading transaction report from " + startDate + " to " + endDate);

                // Test with date filter
                List<LaporanTransaksiItem> data = transactionItemDao.getLaporanTransaksi(startDate, endDate);
                if (startDate == 0 && endDate == 0)
                    data = transactionItemDao.getLaporanTransaksiAll();

                System.out.println("DEBUG: Found " + (data != null ? data.size() : 0) + " transaction items in report with date filter");
                if (data != null) {
                    for (LaporanTransaksiItem item : data) {
                        System.out.println("DEBUG: Report Item - " + item.getNamaProduk() + " - " + item.getJumlahTerjual() + " - " + item.getTotalHarga());
                    }
                }

                liveData.postValue(data);
            } catch (Exception e) {
                System.err.println("DEBUG: Error loading transaction report: " + e.getMessage());
                e.printStackTrace();
                liveData.postValue(new ArrayList<>());
            }
        });
        return liveData;
    }

    // Laporan transaksi dengan filter tanggal dan store
    public LiveData<List<LaporanTransaksiItem>> getLaporanTransaksiByStore(long startDate, long endDate, int storeId) {
        MutableLiveData<List<LaporanTransaksiItem>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<LaporanTransaksiItem> data = transactionItemDao.getLaporanTransaksiByStore(startDate, endDate, storeId);
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
                long transactionId = transactionDao.insert(transaction);
                // Set the generated ID back to the transaction object
                transaction.setId((int) transactionId);
                android.util.Log.d("TransactionRepository", "Transaction inserted with ID: " + transactionId);
                listener.onSuccess();
            } catch (Exception e) {
                android.util.Log.e("TransactionRepository", "Error inserting transaction: " + e.getMessage());
                listener.onError(e.getMessage());
            }
        });
    }

    public void addTransactionItem(TransactionItem item, OnTransactionOperationListener listener) {
        executorService.execute(() -> {
            try {
                android.util.Log.d("TransactionRepository", "Inserting transaction item - Product ID: " + item.getProductId() +
                        ", Transaction ID: " + item.getTransactionId() + ", Quantity: " + item.getQuantity());
                transactionItemDao.insert(item);
                android.util.Log.d("TransactionRepository", "Transaction item inserted successfully");
                listener.onSuccess();
            } catch (Exception e) {
                android.util.Log.e("TransactionRepository", "Error inserting transaction item: " + e.getMessage());
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

    private void createTestData() {
        try {
            // Create a test transaction
            Transaction testTransaction = new Transaction();
            testTransaction.setTotalAmount(150000);
            testTransaction.setPaymentMethod("cash");
            testTransaction.setAmountPaid(200000);
            testTransaction.setChange(50000);
            testTransaction.setStatus("completed");
            testTransaction.setCreatedAt(System.currentTimeMillis());
            testTransaction.setStoreId(1);

            long transactionId = transactionDao.insert(testTransaction);
            System.out.println("DEBUG: Created test transaction with ID: " + transactionId);

            // Create test transaction items
            TransactionItem item1 = new TransactionItem();
            item1.setTransactionId((int) transactionId);
            item1.setProductId(1);
            item1.setProductName("Test Product 1");
            item1.setPrice(50000);
            item1.setQuantity(2);
            item1.setTotal(100000);
            item1.setCreatedAt(System.currentTimeMillis());
            transactionItemDao.insert(item1);

            TransactionItem item2 = new TransactionItem();
            item2.setTransactionId((int) transactionId);
            item2.setProductId(2);
            item2.setProductName("Test Product 2");
            item2.setPrice(25000);
            item2.setQuantity(2);
            item2.setTotal(50000);
            item2.setCreatedAt(System.currentTimeMillis());
            transactionItemDao.insert(item2);

            System.out.println("DEBUG: Created test transaction items");
        } catch (Exception e) {
            System.err.println("DEBUG: Error creating test data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public LiveData<Double> getTodaySalesByStore(Integer storeId) {
        if (storeId == null || storeId == -1) {
            return transactionDao.getTodaySalesAllStores();
        } else {
            return transactionDao.getTodaySalesByStore(storeId);
        }
    }
}
