package id.tugas.pos.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import id.tugas.pos.R;
import id.tugas.pos.data.model.Transaction;
import id.tugas.pos.viewmodel.TransactionViewModel;
import id.tugas.pos.viewmodel.StoreViewModel;
import id.tugas.pos.viewmodel.LoginViewModel;
import id.tugas.pos.data.model.Store;
import id.tugas.pos.data.model.User;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.content.Context;
import androidx.lifecycle.Observer;
import id.tugas.pos.data.repository.TransactionRepository;

public class HistoryFragment extends Fragment implements TransactionHistoryAdapter.OnTransactionClickListener {
    
    private TransactionViewModel transactionViewModel;
    private TransactionHistoryAdapter adapter;
    
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState, tvTotalRevenue, tvTransactionCount;
    private ChipGroup chipGroupFilter;
    
    private String selectedDate = "";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    private StoreViewModel storeViewModel;
    private LoginViewModel loginViewModel;
    private int selectedStoreId = -1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void setupStoreSpinner(Spinner spinner) {
        storeViewModel.getAllStores().observe(getViewLifecycleOwner(), stores -> {
            if (stores == null) return;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            for (Store store : stores) {
                adapter.add(store.getName());
            }
            spinner.setAdapter(adapter);
            // Set selected store if already chosen
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
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int storeId = stores.get(position).getId();
                    storeViewModel.setSelectedStoreId(storeId);
                    selectedStoreId = storeId;
                    // Update observer transaksi sesuai storeId
                    observeTransactionsByStore(storeId);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        });
    }

    private void observeTransactionsByStore(int storeId) {
        // Ganti observer transaksi agar hanya menampilkan transaksi storeId terpilih
        transactionViewModel.getTransactionsByStore(storeId).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                adapter.setTransactions(transactions);
                updateEmptyState(transactions.isEmpty());
                updateSummary(transactions);
            }
        });
    }

    private boolean isAdmin() {
        User user = loginViewModel.getCurrentUser().getValue();
        return user != null && user.isAdmin();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_riwayat, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        storeViewModel = new ViewModelProvider(requireActivity()).get(StoreViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        
        // Initialize views
        initViews(view);
        setupRecyclerView();
        setupFilterChips();
        setupObservers();
        
        // Load recent transactions
        loadRecentTransactions();
    }
    
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvTransactionCount = view.findViewById(R.id.tvTransactionCount);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);
    }
    
    private void setupRecyclerView() {
        adapter = new TransactionHistoryAdapter();
        adapter.setOnTransactionClickListener(this);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }
    
    private void setupFilterChips() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                loadRecentTransactions();
            } else {
                int chipId = checkedIds.get(0);
                if (chipId == R.id.chipToday) {
                    loadTodayTransactions();
                } else if (chipId == R.id.chipWeek) {
                    loadWeekTransactions();
                } else if (chipId == R.id.chipMonth) {
                    loadMonthTransactions();
                } else if (chipId == R.id.chipCustom) {
                    showDatePicker();
                }
            }
        });
    }
    
    private void setupObservers() {
        // Observe recent transactions
        transactionViewModel.getRecentTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                adapter.setTransactions(transactions);
                updateEmptyState(transactions.isEmpty());
                updateSummary(transactions);
            }
        });
        
        // Observe loading state
        transactionViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Observe error messages
        transactionViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showErrorDialog(errorMessage);
            }
        });
    }
    
    private void loadRecentTransactions() {
        // This will trigger the observer for recent transactions
        transactionViewModel.getRecentTransactions();
    }
    
    private void loadTodayTransactions() {
        String today = dateFormat.format(new Date());
        // Note: You'll need to add this method to TransactionViewModel
        // transactionViewModel.getTransactionsByDate(today);
    }
    
    private void loadWeekTransactions() {
        // Calculate week range and load transactions
        // Note: You'll need to add this method to TransactionViewModel
    }
    
    private void loadMonthTransactions() {
        // Calculate month range and load transactions
        // Note: You'll need to add this method to TransactionViewModel
    }
    
    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pilih Tanggal")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Date date = new Date(selection);
            selectedDate = dateFormat.format(date);
            // Load transactions for selected date
            // transactionViewModel.getTransactionsByDate(selectedDate);
        });
        
        datePicker.show(getChildFragmentManager(), "DatePicker");
    }
    
    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }
    
    private void updateSummary(List<Transaction> transactions) {
        double totalRevenue = 0.0;
        for (Transaction transaction : transactions) {
            totalRevenue += transaction.getTotalAmount();
        }
        
        tvTotalRevenue.setText(id.tugas.pos.utils.CurrencyUtils.formatCurrency(totalRevenue));
        tvTransactionCount.setText(String.valueOf(transactions.size()));
    }
    
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
    
    @Override
    public void onTransactionClick(Transaction transaction) {
        showTransactionDetailDialog(transaction);
    }
    
    private void showTransactionDetailDialog(Transaction transaction) {
        // Create a dialog to show transaction details
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Detail Transaksi");
        
        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(transaction.getId()).append("\n");
        details.append("Tanggal: ").append(transaction.getFormattedDate()).append("\n");
        details.append("Total: ").append(id.tugas.pos.utils.CurrencyUtils.formatCurrency(transaction.getTotalAmount())).append("\n");
        details.append("Metode Pembayaran: ").append(transaction.getPaymentMethod()).append("\n");
        details.append("Status: ").append(transaction.getStatus()).append("\n");
        
        if (transaction.getAmountPaid() > 0) {
            details.append("Dibayar: ").append(id.tugas.pos.utils.CurrencyUtils.formatCurrency(transaction.getAmountPaid())).append("\n");
            details.append("Kembalian: ").append(id.tugas.pos.utils.CurrencyUtils.formatCurrency(transaction.getChange())).append("\n");
        }
        
        builder.setMessage(details.toString());
        builder.setPositiveButton("OK", null);
        
        // Add button to change status to COMPLETED if current status is PENDING
        if ("PENDING".equals(transaction.getStatus())) {
            builder.setNeutralButton("Ubah ke COMPLETED", (dialog, which) -> {
                updateTransactionStatus(transaction);
            });
        }
        
        builder.setNegativeButton("Cetak", (dialog, which) -> {
            // TODO: Implement print functionality
        });
        
        builder.show();
    }
    
    private void updateTransactionStatus(Transaction transaction) {
        // Show confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi")
                .setMessage("Apakah Anda yakin ingin mengubah status transaksi #" + transaction.getId() + " menjadi COMPLETED?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    // Update transaction status
                    transaction.setStatus("COMPLETED");
                    transaction.setUpdatedAt(System.currentTimeMillis());
                    
                    transactionViewModel.updateTransaction(transaction, new TransactionRepository.OnTransactionOperationListener() {
                        @Override
                        public void onSuccess() {
                            // Show success message on main thread
                            requireActivity().runOnUiThread(() -> {
                                new AlertDialog.Builder(requireContext())
                                        .setTitle("Sukses")
                                        .setMessage("Status transaksi berhasil diubah menjadi COMPLETED")
                                        .setPositiveButton("OK", null)
                                        .show();
                                
                                // Refresh transaction list
                                loadRecentTransactions();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            // Show error message on main thread
                            requireActivity().runOnUiThread(() -> {
                                new AlertDialog.Builder(requireContext())
                                        .setTitle("Error")
                                        .setMessage("Gagal mengubah status transaksi: " + error)
                                        .setPositiveButton("OK", null)
                                        .show();
                            });
                        }
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }
} 