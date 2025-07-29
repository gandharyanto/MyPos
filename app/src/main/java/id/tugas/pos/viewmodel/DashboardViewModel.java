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
    
    public DashboardViewModel(Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        productRepository = new ProductRepository(application);
        expenseRepository = new ExpenseRepository(application);
        userRepository = new UserRepository(application);
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
        
        // Load total revenue - use observe instead of observeForever
        transactionRepository.getTotalRevenueByStore(storeId).observeForever(revenue -> {
            Log.d(TAG, "loadDashboardDataByStore: Total revenue: " + revenue);
            totalRevenue.setValue(revenue != null ? revenue : 0.0);
            // Recalculate profit margin when revenue changes
            calculateProfitMargin();
        });
        
        // Load today's sales
        loadTodaySales(storeId);
        
        // Load product counts
        productRepository.getActiveProductCountByStore(storeId).observeForever(count -> {
            Log.d(TAG, "loadDashboardDataByStore: Total products: " + count);
            totalProducts.setValue(count != null ? count : 0);
        });
        
        productRepository.getLowStockCountByStore(storeId).observeForever(count -> {
            Log.d(TAG, "loadDashboardDataByStore: Low stock count: " + count);
            lowStockCount.setValue(count != null ? count : 0);
        });
        
        // Load pending transactions
        transactionRepository.getPendingTransactionCountByStore(storeId).observeForever(count -> {
            Log.d(TAG, "loadDashboardDataByStore: Pending transactions: " + count);
            pendingTransactions.setValue(count != null ? count : 0);
        });
        
        // Load total expenses
        expenseRepository.getTotalExpensesByStore(storeId).observeForever(expenses -> {
            Log.d(TAG, "loadDashboardDataByStore: Total expenses: " + expenses);
            totalExpenses.setValue(expenses != null ? expenses : 0.0);
            // Recalculate profit margin when expenses change
            calculateProfitMargin();
        });
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
    
    // Getters for LiveData
    public LiveData<Double> getTotalRevenue() {
        return totalRevenue;
    }
    
    public LiveData<Double> getTodaySales() {
        return todaySales;
    }
    
    public LiveData<Integer> getTotalProducts() {
        return totalProducts;
    }
    
    public LiveData<Integer> getLowStockCount() {
        return lowStockCount;
    }
    
    public LiveData<Integer> getPendingTransactions() {
        return pendingTransactions;
    }
    
    public LiveData<Double> getTotalExpenses() {
        return totalExpenses;
    }
    
    public LiveData<Double> getProfitMargin() {
        return profitMargin;
    }
} 