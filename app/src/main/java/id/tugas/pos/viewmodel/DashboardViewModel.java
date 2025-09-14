package id.tugas.pos.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;

import id.tugas.pos.data.repository.ExpenseRepository;
import id.tugas.pos.data.repository.ProductRepository;
import id.tugas.pos.data.repository.TransactionRepository;
import id.tugas.pos.data.repository.UserRepository;
import id.tugas.pos.data.model.User;

public class DashboardViewModel extends AndroidViewModel {
    
    private static final String TAG = "DashboardViewModel";
    private TransactionRepository transactionRepository;
    private ProductRepository productRepository;
    private ExpenseRepository expenseRepository;
    private UserRepository userRepository;
    
    private MutableLiveData<Double> totalRevenue = new MutableLiveData<>();
    private MutableLiveData<Double> todaySales = new MutableLiveData<>();
    private MutableLiveData<Integer> totalProducts = new MutableLiveData<>();
    private MutableLiveData<Integer> lowStockCount = new MutableLiveData<>();
    private MutableLiveData<Integer> pendingTransactions = new MutableLiveData<>();
    private MutableLiveData<Double> totalExpenses = new MutableLiveData<>();
    private MutableLiveData<Double> profitMargin = new MutableLiveData<>();
    
    private Integer currentStoreId = null;

    // Expose LiveData directly from repositories
    private LiveData<Double> totalRevenueLiveData;
    private LiveData<Double> todaySalesLiveData;
    private LiveData<Integer> totalProductsLiveData;
    private LiveData<Integer> lowStockCountLiveData;
    private LiveData<Integer> pendingTransactionsLiveData;
    private LiveData<Double> totalExpensesLiveData;
    private LiveData<Double> profitMarginLiveData = new MutableLiveData<>();

    // Observer references for cleanup
    private androidx.lifecycle.Observer<Double> revenueObserver;
    private androidx.lifecycle.Observer<Integer> totalProductsObserver;
    private androidx.lifecycle.Observer<Integer> lowStockObserver;
    private androidx.lifecycle.Observer<Integer> pendingTransactionsObserver;
    private androidx.lifecycle.Observer<Double> expensesObserver;

    public DashboardViewModel(Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        productRepository = new ProductRepository(application);
        expenseRepository = new ExpenseRepository(application);
        userRepository = new UserRepository(application);

        // Don't call updateLiveDataSources() here as currentStoreId is null
        // LiveData sources will be initialized when loadDashboardData() is called
    }
    
    public void loadDashboardData() {
        Log.d(TAG, "loadDashboardData: Loading dashboard data");
        // Load dashboard data berdasarkan user role
        // Untuk admin: load semua data (storeId = null)
        // Untuk user: load data berdasarkan storeId user
        loadDashboardDataByUserRole();
    }
    
    private void loadDashboardDataByUserRole() {
        SharedPreferences prefs = getApplication().getSharedPreferences("session", 0);
        int userId = prefs.getInt("userId", -1);
        
        if (userId != -1) {
            userRepository.getUserById(userId).observeForever(user -> {
                if (user != null) {
                    Integer storeId = null;
                    if (user.isUser()) {
                        storeId = user.getStoreId();
                        Log.d(TAG, "loadDashboardDataByUserRole: User storeId: " + storeId);
                    } else {
                        Log.d(TAG, "loadDashboardDataByUserRole: Admin user, loading all stores data");
                    }
                    // Jika admin, storeId tetap null (akan menampilkan data semua store)
                    
                    currentStoreId = storeId;
                    loadDashboardDataByStore(storeId);
                }
            });
        } else {
            // Default load semua data
            Log.d(TAG, "loadDashboardDataByUserRole: No user session, loading all stores data");
            currentStoreId = null;
            loadDashboardDataByStore(null);
        }
    }
    
    // Method untuk load dashboard data berdasarkan storeId tertentu (untuk admin)
    public void loadDashboardDataByStore(Integer storeId) {
        Log.d(TAG, "loadDashboardDataByStore: Loading data for storeId: " + storeId);
        currentStoreId = storeId;
        // Remove previous observers if any
        if (revenueObserver != null) {
            transactionRepository.getTotalRevenueByStore(storeId).removeObserver(revenueObserver);
        }
        if (totalProductsObserver != null) {
            productRepository.getActiveProductCountByStore(storeId).removeObserver(totalProductsObserver);
        }
        if (lowStockObserver != null) {
            productRepository.getLowStockCountByStore(storeId).removeObserver(lowStockObserver);
        }
        if (pendingTransactionsObserver != null) {
            transactionRepository.getPendingTransactionCountByStore(storeId).removeObserver(pendingTransactionsObserver);
        }
        if (expensesObserver != null) {
            expenseRepository.getTotalExpensesByStore(storeId).removeObserver(expensesObserver);
        }
        // Add new observers
        revenueObserver = revenue -> {
            Log.d(TAG, "loadDashboardDataByStore: Total revenue: " + revenue);
            totalRevenue.setValue(revenue != null ? revenue : 0.0);
            calculateProfitMargin();
        };
        transactionRepository.getTotalRevenueByStore(storeId).observeForever(revenueObserver);
        loadTodaySales(storeId);
        totalProductsObserver = count -> {
            Log.d(TAG, "loadDashboardDataByStore: Total products: " + count);
            totalProducts.setValue(count != null ? count : 0);
        };
        productRepository.getActiveProductCountByStore(storeId).observeForever(totalProductsObserver);
        lowStockObserver = count -> {
            Log.d(TAG, "loadDashboardDataByStore: Low stock count: " + count);
            lowStockCount.setValue(count != null ? count : 0);
        };
        productRepository.getLowStockCountByStore(storeId).observeForever(lowStockObserver);
        pendingTransactionsObserver = count -> {
            Log.d(TAG, "loadDashboardDataByStore: Pending transactions: " + count);
            pendingTransactions.setValue(count != null ? count : 0);
        };
        transactionRepository.getPendingTransactionCountByStore(storeId).observeForever(pendingTransactionsObserver);
        expensesObserver = expenses -> {
            Log.d(TAG, "loadDashboardDataByStore: Total expenses: " + expenses);
            totalExpenses.setValue(expenses != null ? expenses : 0.0);
            calculateProfitMargin();
        };
        expenseRepository.getTotalExpensesByStore(storeId).observeForever(expensesObserver);
    }
    
    // Method untuk refresh data setelah transaksi baru
    public void refreshData() {
        Log.d(TAG, "refreshData: Refreshing dashboard data for storeId: " + currentStoreId);
        
        // Force refresh by re-observing the data
        if (currentStoreId != null) {
            loadDashboardDataByStore(currentStoreId);
        } else {
            loadDashboardData();
        }
    }
    
    // Method untuk force refresh specific data
    public void refreshRevenueData() {
        Log.d(TAG, "refreshRevenueData: Refreshing revenue data for storeId: " + currentStoreId);
        
        // Force refresh revenue data
        transactionRepository.getTotalRevenueByStore(currentStoreId).observeForever(revenue -> {
            Log.d(TAG, "refreshRevenueData: New total revenue: " + revenue);
            totalRevenue.setValue(revenue != null ? revenue : 0.0);
            calculateProfitMargin();
        });
        
        // Force refresh today's sales
        loadTodaySales(currentStoreId);
    }
    
    // Method untuk force refresh all data secara manual
    public void forceRefreshAllData() {
        Log.d(TAG, "forceRefreshAllData: Force refreshing all dashboard data for storeId: " + currentStoreId);
        
        // Hanya refresh jika benar-benar diperlukan
        // Untuk sementara, kita skip refresh otomatis untuk menghindari refresh berlebihan
        Log.d(TAG, "forceRefreshAllData: Skipping automatic refresh to prevent excessive updates");
    }
    
    private void loadTodaySales(Integer storeId) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endOfDay = calendar.getTimeInMillis();
        
        transactionRepository.getRevenueByDateRangeAndStore(startOfDay, endOfDay, storeId).observeForever(sales -> {
            Log.d(TAG, "loadTodaySales: Today sales: " + sales);
            todaySales.setValue(sales != null ? sales : 0.0);
        });
    }
    
    private void calculateProfitMargin() {
        // This is a simplified calculation
        // In a real app, you would calculate based on cost price vs selling price
        Double revenue = totalRevenue.getValue();
        Double expenses = totalExpenses.getValue();
        
        if (revenue != null && expenses != null && revenue > 0) {
            double profit = revenue - expenses;
            double margin = (profit / revenue) * 100;
            Log.d(TAG, "calculateProfitMargin: Revenue: " + revenue + ", Expenses: " + expenses + ", Margin: " + margin);
            profitMargin.setValue(margin);
        } else {
            Log.d(TAG, "calculateProfitMargin: Setting margin to 0");
            profitMargin.setValue(0.0);
        }
    }
    
    public void setStoreId(Integer storeId) {
        currentStoreId = storeId;
        updateLiveDataSources();
    }

    private void updateLiveDataSources() {
        // Always initialize LiveData sources regardless of currentStoreId value
        totalRevenueLiveData = transactionRepository.getTotalRevenueByStore(currentStoreId);
        todaySalesLiveData = transactionRepository.getTodaySalesByStore(currentStoreId);
        totalProductsLiveData = productRepository.getActiveProductCountByStore(currentStoreId);
        lowStockCountLiveData = productRepository.getLowStockCountByStore(currentStoreId);
        pendingTransactionsLiveData = transactionRepository.getPendingTransactionCountByStore(currentStoreId);
        totalExpensesLiveData = expenseRepository.getTotalExpensesByStore(currentStoreId);
    }

    // Getters for LiveData - return MutableLiveData that's always initialized
    public LiveData<Double> getTotalRevenue() {
        return totalRevenueLiveData != null ? totalRevenueLiveData : totalRevenue;
    }

    public LiveData<Double> getTodaySales() {
        return todaySalesLiveData != null ? todaySalesLiveData : todaySales;
    }

    public LiveData<Integer> getTotalProducts() {
        return totalProductsLiveData != null ? totalProductsLiveData : totalProducts;
    }

    public LiveData<Integer> getLowStockCount() {
        return lowStockCountLiveData != null ? lowStockCountLiveData : lowStockCount;
    }

    public LiveData<Integer> getPendingTransactions() {
        return pendingTransactionsLiveData != null ? pendingTransactionsLiveData : pendingTransactions;
    }

    public LiveData<Double> getTotalExpenses() {
        return totalExpensesLiveData != null ? totalExpensesLiveData : totalExpenses;
    }

    public LiveData<Double> getProfitMargin() {
        return profitMargin;
    }

    public void clearData() {
        totalRevenue.setValue(null);
        todaySales.setValue(null);
        totalProducts.setValue(null);
        lowStockCount.setValue(null);
        pendingTransactions.setValue(null);
        totalExpenses.setValue(null);
        profitMargin.setValue(null);
        // Optionally, clear repository cache if any
    }
}
