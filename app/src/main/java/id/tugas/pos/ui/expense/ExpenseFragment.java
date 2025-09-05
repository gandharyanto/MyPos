package id.tugas.pos.ui.expense;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import id.tugas.pos.viewmodel.StoreViewModel;
import id.tugas.pos.viewmodel.LoginViewModel;
import id.tugas.pos.data.model.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import id.tugas.pos.R;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import id.tugas.pos.data.model.Expense;
import id.tugas.pos.viewmodel.ExpenseViewModel;
import android.widget.ProgressBar;
import java.util.List;
import android.widget.ArrayAdapter;
import android.widget.Button;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.util.Calendar;

public class ExpenseFragment extends Fragment {
    
    private StoreViewModel storeViewModel;
    private LoginViewModel loginViewModel;
    private ExpenseViewModel expenseViewModel;
    private ExpenseAdapter expenseAdapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState, tvTotalExpenses, tvExpenseCount;
    private int selectedStoreId = -1;
    private Spinner spinnerStore; // Tambahkan deklarasi Spinner
    private long startDate = 0;
    private long endDate = 0;
    private ChipGroup chipGroupFilter;
    private Chip chipToday, chipWeek, chipMonth, chipCustom;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);
        storeViewModel = new ViewModelProvider(requireActivity()).get(StoreViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        initViews(view);
        setupRecyclerView();
        if (isAdmin()) {
            spinnerStore.setVisibility(View.VISIBLE);
            setupStoreSpinner(spinnerStore);
        } else {
            spinnerStore.setVisibility(View.GONE);
        }
        setupObservers(view);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);
        chipToday = view.findViewById(R.id.chipToday);
        chipWeek = view.findViewById(R.id.chipWeek);
        chipMonth = view.findViewById(R.id.chipMonth);
        chipCustom = view.findViewById(R.id.chipCustom);
        setupChipListeners();
        // Set default filter to today
        setTodayFilter();
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses);
        tvExpenseCount = view.findViewById(R.id.tvExpenseCount);
        Button btnTambahPengeluaran = view.findViewById(R.id.btnTambahPengeluaran);
        spinnerStore = view.findViewById(R.id.spinnerStore); // Inisialisasi spinner
        btnTambahPengeluaran.setOnClickListener(v -> {
            new ExpenseDialogFragment(selectedStoreId).show(getParentFragmentManager(), "expense_dialog");
        });
    }

    private void setupRecyclerView() {
        expenseAdapter = new ExpenseAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(expenseAdapter);
    }

    private void setupObservers(View view) {
        Button btnTambahPengeluaran = view.findViewById(R.id.btnTambahPengeluaran);
        if (isAdmin()) {
            storeViewModel.getSelectedStoreId().observe(getViewLifecycleOwner(), storeId -> {
                if (storeId != null && storeId >= 0) {
                    selectedStoreId = storeId;
                    btnTambahPengeluaran.setEnabled(true);
                    observeExpensesByStore(storeId);
                } else {
                    btnTambahPengeluaran.setEnabled(false);
                }
            });
        } else {
            User user = loginViewModel.getCurrentUser().getValue();
            if (user != null && user.getStoreId() != null) {
                selectedStoreId = user.getStoreId();
                btnTambahPengeluaran.setEnabled(true);
                observeExpensesByStore(selectedStoreId);
            } else {
                btnTambahPengeluaran.setEnabled(false);
            }
        }
    }

    private void observeExpensesByStore(int storeId) {
        expenseViewModel.getAllExpensesByStore(storeId).observe(getViewLifecycleOwner(), expenses -> {
            expenseAdapter.setExpenses(expenses);
            updateEmptyState(expenses == null || expenses.isEmpty());
            updateSummary(expenses);
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void updateSummary(List<Expense> expenses) {
        double total = 0.0;
        int count = 0;
        if (expenses != null) {
            count = expenses.size();
            for (Expense e : expenses) total += e.getAmount();
        }
        tvTotalExpenses.setText("Rp " + String.format(java.util.Locale.getDefault(), "%,.0f", total));
        tvExpenseCount.setText(String.valueOf(count));
    }

    private void setupStoreSpinner(Spinner spinner) {
        storeViewModel.getAllStores().observe(getViewLifecycleOwner(), stores -> {
            if (stores == null) return;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_black_text);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_black_text);
            for (id.tugas.pos.data.model.Store store : stores) {
                adapter.add(store.getName());
            }
            spinner.setAdapter(adapter);
            Integer currentStoreId = storeViewModel.getSelectedStoreId().getValue();
            if (currentStoreId != null && currentStoreId >= 0) {
                int pos = 0;
                for (int i = 0; i < stores.size(); i++) {
                    if (stores.get(i).getId() == currentStoreId) {
                        pos = i;
                        break;
                    }
                }
                spinner.setSelection(pos);
            }
            spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    int storeId = stores.get(position).getId();
                    storeViewModel.setSelectedStoreId(storeId);
                    selectedStoreId = storeId;
                    observeExpensesByStore(storeId);
                }
                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        });
    }

    private void setupChipListeners() {
        chipToday.setOnClickListener(v -> setTodayFilter());
        chipWeek.setOnClickListener(v -> setWeekFilter());
        chipMonth.setOnClickListener(v -> setMonthFilter());
        chipCustom.setOnClickListener(v -> showCustomDatePicker());
    }

    private void setTodayFilter() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        startDate = cal.getTimeInMillis();
        endDate = startDate + 24 * 60 * 60 * 1000 - 1;
        observeExpensesByStoreAndDate(selectedStoreId, startDate, endDate);
    }

    private void setWeekFilter() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        startDate = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_WEEK, 6);
        endDate = cal.getTimeInMillis() + 24 * 60 * 60 * 1000 - 1;
        observeExpensesByStoreAndDate(selectedStoreId, startDate, endDate);
    }

    private void setMonthFilter() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        startDate = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        endDate = cal.getTimeInMillis() - 1;
        observeExpensesByStoreAndDate(selectedStoreId, startDate, endDate);
    }

    private void showCustomDatePicker() {
        MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Pilih Rentang Tanggal");
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            startDate = selection.first;
            // Set endDate to the end of the last day
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selection.second);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            endDate = cal.getTimeInMillis();
            observeExpensesByStoreAndDate(selectedStoreId, startDate, endDate);
        });
        picker.show(getParentFragmentManager(), "date_range_picker");
    }

    private void observeExpensesByStoreAndDate(int storeId, long startDate, long endDate) {
        expenseViewModel.getExpensesByDateRangeAndStore(startDate, endDate, storeId).observe(getViewLifecycleOwner(), expenses -> {
            expenseAdapter.setExpenses(expenses);
            updateEmptyState(expenses == null || expenses.isEmpty());
            updateSummary(expenses);
        });
    }

    private boolean isAdmin() {
        User user = loginViewModel.getCurrentUser().getValue();
        return user != null && user.isAdmin();
    }
}
