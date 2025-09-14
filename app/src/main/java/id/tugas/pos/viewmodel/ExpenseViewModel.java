package id.tugas.pos.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import id.tugas.pos.data.model.Expense;
import id.tugas.pos.data.repository.ExpenseRepository;
import id.tugas.pos.utils.ModalAwalManager;

public class ExpenseViewModel extends AndroidViewModel {
    private ExpenseRepository expenseRepository;
    private ModalAwalManager modalAwalManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        expenseRepository = new ExpenseRepository(application);
        modalAwalManager = new ModalAwalManager(application);
    }

    public LiveData<List<Expense>> getAllExpenses() {
        return expenseRepository.getAllExpenses();
    }

    public LiveData<List<Expense>> getAllExpensesByStore(int storeId) {
        return expenseRepository.getAllExpensesByStore(storeId);
    }

    public LiveData<Double> getTotalExpenses() {
        return expenseRepository.getTotalExpenses();
    }

    public LiveData<Integer> getExpenseCount() {
        return expenseRepository.getExpenseCount();
    }

    public LiveData<Double> getTotalExpensesByStore(int storeId) {
        return expenseRepository.getTotalExpensesByStore(storeId);
    }

    public LiveData<Integer> getExpenseCountByStore(int storeId) {
        return expenseRepository.getExpenseCountByStore(storeId);
    }

    public LiveData<List<Expense>> getExpensesByDateRangeAndStore(long startDate, long endDate, int storeId) {
        return expenseRepository.getExpensesByDateRangeAndStore(startDate, endDate, storeId);
    }

    public void insert(Expense expense, Runnable onSuccess) {
        executor.execute(() -> {
            expenseRepository.insert(expense);

            // Notify modal awal manager about new expense
            if (expense.getStoreId() > 0) {
                modalAwalManager.onExpenseAdded(expense.getStoreId(), expense.getAmount());
            }

            if (onSuccess != null) onSuccess.run();
        });
    }

    // Method to get modal manager for fragments that need it
    public ModalAwalManager getModalAwalManager() {
        return modalAwalManager;
    }
}
