package id.tugas.pos.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import id.tugas.pos.data.model.Expense;
import id.tugas.pos.data.repository.ExpenseRepository;

public class ExpenseViewModel extends AndroidViewModel {
    private ExpenseRepository expenseRepository;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        expenseRepository = new ExpenseRepository(application);
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
} 