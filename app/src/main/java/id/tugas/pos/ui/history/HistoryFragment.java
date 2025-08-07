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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import id.tugas.pos.R;
import id.tugas.pos.data.model.Transaction;
import id.tugas.pos.ui.transaksi.TransaksiViewModel;
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
    
    private TransaksiViewModel transactionViewModel;
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
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_black_text);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_black_text);
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
                    // Update observer transaksi sesuai storeId dan filter yang aktif
                    observeTransactionsByStore(storeId);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        });
    }

    private void observeTransactionsByStore(int storeId) {
        // Update selected store ID and reload current filter
        selectedStoreId = storeId;
        
        // Get current filter selection and reload
        int selectedChipId = chipGroupFilter.getCheckedChipId();
        
        if (selectedChipId == R.id.chipAll) {
            loadRecentTransactions();
        } else if (selectedChipId == R.id.chipToday) {
            loadTodayTransactions();
        } else if (selectedChipId == R.id.chipWeek) {
            loadWeekTransactions();
        } else if (selectedChipId == R.id.chipMonth) {
            loadMonthTransactions();
        } else if (selectedChipId == R.id.chipCustom) {
            if (!selectedDate.isEmpty()) {
                try {
                    Date date = dateFormat.parse(selectedDate);
                    
                    // Calculate start and end of selected day using Calendar
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    long startOfDay = calendar.getTimeInMillis();
                    
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    long endOfDay = calendar.getTimeInMillis();
                    
                    transactionViewModel.getTransactionsByDateRange(startOfDay, endOfDay).observe(getViewLifecycleOwner(), transactions -> {
                        if (transactions != null) {
                            // Filter transactions by store
                            List<Transaction> filteredTransactions = new ArrayList<>();
                            for (Transaction transaction : transactions) {
                                if (transaction.getStoreId() == selectedStoreId) {
                                    filteredTransactions.add(transaction);
                                }
                            }
                            adapter.setTransactions(filteredTransactions);
                            updateEmptyState(filteredTransactions.isEmpty());
                            updateSummary(filteredTransactions);
                        }
                    });
                } catch (Exception e) {
                    System.out.println("DEBUG: Error parsing selected date: " + e.getMessage());
                }
            }
        } else {
            // Default to all recent transactions
            loadRecentTransactions();
        }
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
        transactionViewModel = new ViewModelProvider(this).get(TransaksiViewModel.class);
        storeViewModel = new ViewModelProvider(requireActivity()).get(StoreViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        
        // Initialize selected store ID
        Integer currentStoreId = storeViewModel.getSelectedStoreId().getValue();
        selectedStoreId = currentStoreId != null ? currentStoreId : -1;
        
        // Initialize views
        initViews(view);
        setupRecyclerView();
        setupFilterChips();
        setupObservers();
        
        // Load recent transactions (default to today)
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
        // Set default selection to "Semua" to show all transactions
        chipGroupFilter.check(R.id.chipAll);
        System.out.println("DEBUG: Default chip selected: chipAll");
        
        // Add individual click listeners to each chip
        chipGroupFilter.findViewById(R.id.chipAll).setOnClickListener(v -> {
            System.out.println("DEBUG: Chip All clicked");
            chipGroupFilter.check(R.id.chipAll);
            loadRecentTransactions();
        });
        
        chipGroupFilter.findViewById(R.id.chipToday).setOnClickListener(v -> {
            System.out.println("DEBUG: Chip Today clicked");
            chipGroupFilter.check(R.id.chipToday);
            loadTodayTransactions();
        });
        
        chipGroupFilter.findViewById(R.id.chipWeek).setOnClickListener(v -> {
            System.out.println("DEBUG: Chip Week clicked");
            chipGroupFilter.check(R.id.chipWeek);
            loadWeekTransactions();
        });
        
        chipGroupFilter.findViewById(R.id.chipMonth).setOnClickListener(v -> {
            System.out.println("DEBUG: Chip Month clicked");
            chipGroupFilter.check(R.id.chipMonth);
            loadMonthTransactions();
        });
        
        chipGroupFilter.findViewById(R.id.chipCustom).setOnClickListener(v -> {
            System.out.println("DEBUG: Chip Custom clicked");
            chipGroupFilter.check(R.id.chipCustom);
            showDatePicker();
        });
        
        // Keep the original listener as backup
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            System.out.println("DEBUG: Chip selection changed. Checked IDs: " + checkedIds);
            
            if (checkedIds.isEmpty()) {
                // If no chip is selected, default to "Semua"
                System.out.println("DEBUG: No chip selected, defaulting to all transactions");
                chipGroupFilter.check(R.id.chipAll);
                loadRecentTransactions();
            } else {
                int chipId = checkedIds.get(0);
                System.out.println("DEBUG: Selected chip ID: " + chipId);
                
                if (chipId == R.id.chipAll) {
                    System.out.println("DEBUG: Loading all recent transactions");
                    loadRecentTransactions();
                } else if (chipId == R.id.chipToday) {
                    System.out.println("DEBUG: Loading today transactions");
                    loadTodayTransactions();
                } else if (chipId == R.id.chipWeek) {
                    System.out.println("DEBUG: Loading week transactions");
                    loadWeekTransactions();
                } else if (chipId == R.id.chipMonth) {
                    System.out.println("DEBUG: Loading month transactions");
                    loadMonthTransactions();
                } else if (chipId == R.id.chipCustom) {
                    System.out.println("DEBUG: Showing date picker");
                    showDatePicker();
                }
            }
        });
    }
    
    private void setupObservers() {
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
        System.out.println("DEBUG: === loadRecentTransactions() called ===");
        // Default to all recent transactions instead of just today
        System.out.println("DEBUG: Loading all recent transactions");
        transactionViewModel.getRecentTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                System.out.println("DEBUG: Recent transactions loaded: " + transactions.size());
                adapter.setTransactions(transactions);
                updateEmptyState(transactions.isEmpty());
                updateSummary(transactions);
            }
        });
    }
    
    private void loadTodayTransactions() {
        System.out.println("DEBUG: === loadTodayTransactions() called ===");
        
        // Calculate today's start and end timestamps using Calendar
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = calendar.getTimeInMillis();
        
        long now = System.currentTimeMillis();
        
        String today = dateFormat.format(new Date());
        System.out.println("DEBUG: Loading today transactions for date: " + today + ", storeId: " + selectedStoreId);
        System.out.println("DEBUG: Start of day: " + startOfDay + ", End of day: " + endOfDay);
        System.out.println("DEBUG: Current time: " + now);
        
        // First, let's check all transactions to see their timestamps
        transactionViewModel.getRecentTransactions().observe(getViewLifecycleOwner(), allTransactions -> {
            if (allTransactions != null) {
                System.out.println("DEBUG: All transactions in database: " + allTransactions.size());
                for (Transaction t : allTransactions) {
                    System.out.println("DEBUG: Transaction ID: " + t.getId() + 
                                     ", CreatedAt: " + t.getCreatedAt() + 
                                     ", Formatted: " + t.getFormattedDate() + 
                                     ", StoreId: " + t.getStoreId());
                }
            }
        });
        
        if (selectedStoreId >= 0) {
            // Filter by store and date
            System.out.println("DEBUG: Observing transactions with store filter");
            transactionViewModel.getTransactionsByDateRange(startOfDay, endOfDay).observe(getViewLifecycleOwner(), transactions -> {
                System.out.println("DEBUG: Today transactions received: " + (transactions != null ? transactions.size() : 0));
                if (transactions != null) {
                    // Filter transactions by store
                    List<Transaction> filteredTransactions = new ArrayList<>();
                    for (Transaction transaction : transactions) {
                        if (transaction.getStoreId() == selectedStoreId) {
                            filteredTransactions.add(transaction);
                        }
                    }
                    System.out.println("DEBUG: Filtered transactions for store: " + filteredTransactions.size());
                    adapter.setTransactions(filteredTransactions);
                    updateEmptyState(filteredTransactions.isEmpty());
                    updateSummary(filteredTransactions);
                }
            });
        } else {
            System.out.println("DEBUG: Observing transactions without store filter");
            transactionViewModel.getTransactionsByDateRange(startOfDay, endOfDay).observe(getViewLifecycleOwner(), transactions -> {
                System.out.println("DEBUG: Today transactions received (no store filter): " + (transactions != null ? transactions.size() : 0));
                if (transactions != null) {
                    adapter.setTransactions(transactions);
                    updateEmptyState(transactions.isEmpty());
                    updateSummary(transactions);
                }
            });
        }
    }
    
    private void loadWeekTransactions() {
        System.out.println("DEBUG: === loadWeekTransactions() called ===");
        
        // Calculate week range (7 days from today)
        long endDate = System.currentTimeMillis();
        long startDate = endDate - (7 * 24 * 60 * 60 * 1000L); // 7 days ago
        
        System.out.println("DEBUG: Loading week transactions from " + startDate + " to " + endDate + ", storeId: " + selectedStoreId);
        
        transactionViewModel.getTransactionsByDateRange(startDate, endDate).observe(getViewLifecycleOwner(), transactions -> {
            System.out.println("DEBUG: Week transactions received: " + (transactions != null ? transactions.size() : 0));
            if (transactions != null) {
                if (selectedStoreId >= 0) {
                    // Filter transactions by store
                    List<Transaction> filteredTransactions = new ArrayList<>();
                    for (Transaction transaction : transactions) {
                        if (transaction.getStoreId() == selectedStoreId) {
                            filteredTransactions.add(transaction);
                        }
                    }
                    System.out.println("DEBUG: Filtered week transactions for store: " + filteredTransactions.size());
                    adapter.setTransactions(filteredTransactions);
                    updateEmptyState(filteredTransactions.isEmpty());
                    updateSummary(filteredTransactions);
                } else {
                    adapter.setTransactions(transactions);
                    updateEmptyState(transactions.isEmpty());
                    updateSummary(transactions);
                }
            }
        });
    }
    
    private void loadMonthTransactions() {
        System.out.println("DEBUG: === loadMonthTransactions() called ===");
        
        // Calculate month range (30 days from today)
        long endDate = System.currentTimeMillis();
        long startDate = endDate - (30 * 24 * 60 * 60 * 1000L); // 30 days ago
        
        System.out.println("DEBUG: Loading month transactions from " + startDate + " to " + endDate + ", storeId: " + selectedStoreId);
        
        transactionViewModel.getTransactionsByDateRange(startDate, endDate).observe(getViewLifecycleOwner(), transactions -> {
            System.out.println("DEBUG: Month transactions received: " + (transactions != null ? transactions.size() : 0));
            if (transactions != null) {
                if (selectedStoreId >= 0) {
                    // Filter transactions by store
                    List<Transaction> filteredTransactions = new ArrayList<>();
                    for (Transaction transaction : transactions) {
                        if (transaction.getStoreId() == selectedStoreId) {
                            filteredTransactions.add(transaction);
                        }
                    }
                    System.out.println("DEBUG: Filtered month transactions for store: " + filteredTransactions.size());
                    adapter.setTransactions(filteredTransactions);
                    updateEmptyState(filteredTransactions.isEmpty());
                    updateSummary(filteredTransactions);
                } else {
                    adapter.setTransactions(transactions);
                    updateEmptyState(transactions.isEmpty());
                    updateSummary(transactions);
                }
            }
        });
    }
    
    private void showDatePicker() {
        System.out.println("DEBUG: === showDatePicker() called ===");
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pilih Tanggal")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Date date = new Date(selection);
            selectedDate = dateFormat.format(date);
            
            // Calculate start and end of selected day using Calendar
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startOfDay = calendar.getTimeInMillis();
            
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            long endOfDay = calendar.getTimeInMillis();
            
            System.out.println("DEBUG: Date selected: " + selectedDate + ", storeId: " + selectedStoreId);
            System.out.println("DEBUG: Start of selected day: " + startOfDay + ", End of selected day: " + endOfDay);
            
            // Load transactions for selected date
            transactionViewModel.getTransactionsByDateRange(startOfDay, endOfDay).observe(getViewLifecycleOwner(), transactions -> {
                System.out.println("DEBUG: Custom date transactions received: " + (transactions != null ? transactions.size() : 0));
                if (transactions != null) {
                    if (selectedStoreId >= 0) {
                        // Filter transactions by store
                        List<Transaction> filteredTransactions = new ArrayList<>();
                        for (Transaction transaction : transactions) {
                            if (transaction.getStoreId() == selectedStoreId) {
                                filteredTransactions.add(transaction);
                            }
                        }
                        System.out.println("DEBUG: Filtered custom date transactions for store: " + filteredTransactions.size());
                        adapter.setTransactions(filteredTransactions);
                        updateEmptyState(filteredTransactions.isEmpty());
                        updateSummary(filteredTransactions);
                    } else {
                        adapter.setTransactions(transactions);
                        updateEmptyState(transactions.isEmpty());
                        updateSummary(transactions);
                    }
                }
            });
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
        
        System.out.println("DEBUG: Updating summary - Transactions: " + transactions.size() + ", Total Revenue: " + totalRevenue);
        
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