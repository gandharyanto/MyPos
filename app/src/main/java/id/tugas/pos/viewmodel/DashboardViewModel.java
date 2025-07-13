package id.tugas.pos.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;

import id.tugas.pos.data.repository.ExpenseRepository;
import id.tugas.pos.data.repository.ProductRepository;
import id.tugas.pos.data.repository.TransactionRepository;

public class DashboardViewModel extends AndroidViewModel {
    
    private TransactionRepository transactionRepository;
    private ProductRepository productRepository;
    private ExpenseRepository expenseRepository;
    
    private MutableLiveData<Double> totalRevenue = new MutableLiveData<>();
    private MutableLiveData<Double> todaySales = new MutableLiveData<>();
    private MutableLiveData<Integer> totalProducts = new MutableLiveData<>();
    private MutableLiveData<Integer> lowStockCount = new MutableLiveData<>();
    private MutableLiveData<Integer> pendingTransactions = new MutableLiveData<>();
    private MutableLiveData<Double> totalExpenses = new MutableLiveData<>();
    private MutableLiveData<Double> profitMargin = new MutableLiveData<>();
    
    public DashboardViewModel(Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        productRepository = new ProductRepository(application);
        expenseRepository = new ExpenseRepository(application);
    }
    
    public void loadDashboardData() {
        // Load total revenue
        transactionRepository.getTotalRevenue().observeForever(revenue -> {
            totalRevenue.setValue(revenue != null ? revenue : 0.0);
        });
        
        // Load today's sales
        loadTodaySales();
        
        // Load product counts
        productRepository.getActiveProductCount().observeForever(count -> {
            totalProducts.setValue(count != null ? count : 0);
        });
        
        productRepository.getLowStockCount().observeForever(count -> {
            lowStockCount.setValue(count != null ? count : 0);
        });
        
        // Load pending transactions
        transactionRepository.getPendingTransactionCount().observeForever(count -> {
            pendingTransactions.setValue(count != null ? count : 0);
        });
        
        // Load total expenses
        expenseRepository.getTotalExpenses().observeForever(expenses -> {
            totalExpenses.setValue(expenses != null ? expenses : 0.0);
        });
        
        // Calculate profit margin
        calculateProfitMargin();
    }
    
    private void loadTodaySales() {
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
        
        transactionRepository.getRevenueByDateRange(startOfDay, endOfDay).observeForever(sales -> {
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
            profitMargin.setValue(margin);
        } else {
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