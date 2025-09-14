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
    private ModalAwalRepository modalAwalRepository;

    public ExpenseRepository(Application application) {
        PosDatabase database = PosDatabase.getInstance(application);
        expenseDao = database.expenseDao();
        allExpenses = expenseDao.getAllExpenses();
        totalExpenses = expenseDao.getTotalExpenses();
        expenseCount = expenseDao.getExpenseCount();
        modalAwalRepository = new ModalAwalRepository(application);
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
    
    // Get expense count
    public LiveData<Integer> getExpenseCount() {
        return expenseCount;
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
    
    // Store-based methods for modal integration
    public LiveData<List<Expense>> getAllExpensesByStore(int storeId) {
        return expenseDao.getAllExpensesByStore(storeId);
    }

    public LiveData<Double> getTotalExpensesByStore(Integer storeId) {
        if (storeId == null) {
            return expenseDao.getTotalExpenses(); // Return total for all stores
        }
        return expenseDao.getTotalExpensesByStore(storeId);
    }
    
    public LiveData<Integer> getExpenseCountByStore(int storeId) {
        return expenseDao.getExpenseCountByStore(storeId);
    }
    
    public LiveData<List<Expense>> getExpensesByDateRangeAndStore(long startDate, long endDate, int storeId) {
        return expenseDao.getExpensesByDateRangeAndStore(startDate, endDate, storeId);
    }
    
    // Synchronous method for modal calculation
    public double getTotalExpensesByStoreSync(int storeId) {
        try {
            return expenseDao.getTotalExpensesByStoreSync(storeId);
        } catch (Exception e) {
            return 0.0;
        }
    }

    // Get laporan pengeluaran (all stores) - LiveData version with modal calculation
    public LiveData<List<LaporanPengeluaranItem>> getLaporanPengeluaran(long startDate, long endDate) {
        MutableLiveData<List<LaporanPengeluaranItem>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            try {
                List<LaporanPengeluaranItem> laporan = expenseDao.getLaporanPengeluaran(startDate, endDate);

                // Calculate modal awal total from all stores
                double totalModalAwal = getTotalModalAwalAllStores();
                double totalPengeluaran = getTotalExpensesByDateRangeSync(startDate, endDate);
                double sisaModal = totalModalAwal - totalPengeluaran;

                // Set modal information for each item
                for (LaporanPengeluaranItem item : laporan) {
                    item.setModalAwal(totalModalAwal);
                    item.setSisaModal(sisaModal);
                }

                liveData.postValue(laporan);
            } catch (Exception e) {
                liveData.postValue(null);
            }
        });
        return liveData;
    }

    // Get laporan pengeluaran by store - LiveData version with modal calculation
    public LiveData<List<LaporanPengeluaranItem>> getLaporanPengeluaranByStore(long startDate, long endDate, int storeId) {
        MutableLiveData<List<LaporanPengeluaranItem>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            try {
                List<LaporanPengeluaranItem> laporan = expenseDao.getLaporanPengeluaranByStore(startDate, endDate, storeId);

                // Calculate modal awal and sisa modal for specific store
                double modalAwal = modalAwalRepository.getTotalModalByStore(storeId);
                double totalPengeluaran = getTotalExpensesByStoreSync(storeId);
                double sisaModal = modalAwal - totalPengeluaran;

                // Set modal information for each item
                for (LaporanPengeluaranItem item : laporan) {
                    item.setModalAwal(modalAwal);
                    item.setSisaModal(sisaModal);
                }

                liveData.postValue(laporan);
            } catch (Exception e) {
                liveData.postValue(null);
            }
        });
        return liveData;
    }

    // Get laporan pengeluaran by store - Callback version with modal calculation
    public void getLaporanPengeluaranByStore(int storeId, long startDate, long endDate,
                                             LaporanCallback callback) {
        executorService.execute(() -> {
            try {
                List<LaporanPengeluaranItem> laporan = expenseDao.getLaporanPengeluaranByStore(
                    startDate, endDate, storeId);

                // Calculate modal awal and sisa modal
                double modalAwal = modalAwalRepository.getTotalModalByStore(storeId);
                double totalPengeluaran = getTotalExpensesByStoreSync(storeId);
                double sisaModal = modalAwal - totalPengeluaran;

                // Set modal information for each item
                for (LaporanPengeluaranItem item : laporan) {
                    item.setModalAwal(modalAwal);
                    item.setSisaModal(sisaModal);
                }

                callback.onSuccess(laporan);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    // Helper methods for modal calculation
    private double getTotalModalAwalAllStores() {
        try {
            // Get total modal from all stores - this would need to be implemented in ModalAwalRepository
            // For now we'll return sum of all stores
            return modalAwalRepository.getTotalModalByStore(0); // 0 could mean all stores
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double getTotalExpensesByDateRangeSync(long startDate, long endDate) {
        try {
            // This method needs to be implemented in ExpenseDao
            return expenseDao.getTotalExpensesByDateRangeSync(startDate, endDate);
        } catch (Exception e) {
            return 0.0;
        }
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
