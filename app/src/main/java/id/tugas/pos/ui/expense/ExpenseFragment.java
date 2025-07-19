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
import id.tugas.pos.ui.saving.SavingDialogFragment;
import android.widget.Button;

public class ExpenseFragment extends Fragment {
    
    private StoreViewModel storeViewModel;
    private LoginViewModel loginViewModel;
    private ExpenseViewModel expenseViewModel;
    private ExpenseAdapter expenseAdapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState, tvTotalExpenses, tvExpenseCount;
    private int selectedStoreId = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        setupObservers();
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses);
        tvExpenseCount = view.findViewById(R.id.tvExpenseCount);
        Button btnTambahPengeluaran = view.findViewById(R.id.btnTambahPengeluaran);
        btnTambahPengeluaran.setOnClickListener(v -> {
            new SavingDialogFragment().show(getParentFragmentManager(), "saving_dialog");
        });
    }

    private void setupRecyclerView() {
        expenseAdapter = new ExpenseAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(expenseAdapter);
    }

    private void setupObservers() {
        if (isAdmin()) {
            storeViewModel.getSelectedStoreId().observe(getViewLifecycleOwner(), storeId -> {
                if (storeId != null && storeId >= 0) {
                    selectedStoreId = storeId;
                    observeExpensesByStore(storeId);
                }
            });
        } else {
            User user = loginViewModel.getCurrentUser().getValue();
            if (user != null && user.getStoreId() != null) {
                selectedStoreId = user.getStoreId();
                observeExpensesByStore(selectedStoreId);
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
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
        if (isAdmin()) {
            MenuItem item = menu.findItem(R.id.action_store_spinner);
            if (item != null) {
                Spinner spinner = item.getActionView().findViewById(R.id.spinnerStore);
                setupStoreSpinner(spinner);
            }
        } else {
            MenuItem item = menu.findItem(R.id.action_store_spinner);
            if (item != null) item.setVisible(false);
        }
    }

    private boolean isAdmin() {
        User user = loginViewModel.getCurrentUser().getValue();
        return user != null && user.isAdmin();
    }
} 