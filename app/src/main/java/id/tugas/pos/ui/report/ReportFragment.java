package id.tugas.pos.ui.report;

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
import id.tugas.pos.viewmodel.TransactionViewModel;
import id.tugas.pos.viewmodel.ExpenseViewModel;
import id.tugas.pos.data.model.User;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import id.tugas.pos.R;

public class ReportFragment extends Fragment {
    
    private StoreViewModel storeViewModel;
    private LoginViewModel loginViewModel;
    private TransactionViewModel transactionViewModel;
    private ExpenseViewModel expenseViewModel;
    private int selectedStoreId = -1;
    private TextView tvTotalSales, tvTotalExpenses, tvProfit, tvTransactionCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        storeViewModel = new ViewModelProvider(requireActivity()).get(StoreViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        initViews(view);
        setupObservers();
        return view;
    }

    private void initViews(View view) {
        tvTotalSales = view.findViewById(R.id.tvTotalSales);
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses);
        tvProfit = view.findViewById(R.id.tvProfit);
        tvTransactionCount = view.findViewById(R.id.tvTransactionCount);
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
                    observeSummaryByStore(storeId);
                }
                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        });
    }

    private void setupObservers() {
        if (isAdmin()) {
            storeViewModel.getSelectedStoreId().observe(getViewLifecycleOwner(), storeId -> {
                if (storeId != null && storeId >= 0) {
                    selectedStoreId = storeId;
                    observeSummaryByStore(storeId);
                }
            });
        } else {
            User user = loginViewModel.getCurrentUser().getValue();
            if (user != null && user.getStoreId() != null) {
                selectedStoreId = user.getStoreId();
                observeSummaryByStore(selectedStoreId);
            }
        }
    }

    private void observeSummaryByStore(int storeId) {
        // Total Penjualan
        transactionViewModel.getAllTransactionsByStore(storeId).observe(getViewLifecycleOwner(), transactions -> {
            double totalSales = 0.0;
            int transactionCount = 0;
            if (transactions != null) {
                transactionCount = transactions.size();
                for (id.tugas.pos.data.model.Transaction t : transactions) {
                    if ("COMPLETED".equalsIgnoreCase(t.getStatus())) {
                        totalSales += t.getTotalAmount();
                    }
                }
            }
            tvTotalSales.setText("Rp " + String.format(java.util.Locale.getDefault(), "%,.0f", totalSales));
            tvTransactionCount.setText(String.valueOf(transactionCount));
            // Profit akan diupdate setelah total expenses didapat
        });
        // Total Pengeluaran
        expenseViewModel.getTotalExpensesByStore(storeId).observe(getViewLifecycleOwner(), totalExpenses -> {
            double expenses = totalExpenses != null ? totalExpenses : 0.0;
            tvTotalExpenses.setText("Rp " + String.format(java.util.Locale.getDefault(), "%,.0f", expenses));
            // Profit = totalSales - expenses
            String salesStr = tvTotalSales.getText().toString().replace("Rp ", "").replace(".", "").replace(",", "");
            double sales = 0.0;
            try { sales = Double.parseDouble(salesStr); } catch (Exception ignored) {}
            double profit = sales - expenses;
            tvProfit.setText("Rp " + String.format(java.util.Locale.getDefault(), "%,.0f", profit));
        });
    }

    private boolean isAdmin() {
        User user = loginViewModel.getCurrentUser().getValue();
        return user != null && user.isAdmin();
    }
} 