package id.tugas.pos.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.AdapterView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import id.tugas.pos.R;
import id.tugas.pos.viewmodel.DashboardViewModel;
import id.tugas.pos.viewmodel.LoginViewModel;
import id.tugas.pos.data.model.Store;
import id.tugas.pos.data.model.User;
import id.tugas.pos.ui.MainActivity;
import id.tugas.pos.data.model.ModalAwal;
import id.tugas.pos.data.repository.ModalAwalRepository;
import java.util.Calendar;

public class DashboardFragment extends Fragment {
    
    private static final String TAG = "DashboardFragment";
    private DashboardViewModel dashboardViewModel;
    private LoginViewModel loginViewModel;
    private TextView tvTotalRevenue, tvTodaySales, tvTotalProducts, tvLowStockCount;
    private TextView tvPendingTransactions, tvTotalExpenses, tvProfitMargin;
    private TextView tvModalAwal;
    private MainActivity mainActivity;
    private ModalAwalRepository modalAwalRepository;
    
    // Quick action buttons
    private com.google.android.material.button.MaterialButton btnQuickTransaction, btnQuickProduct, btnQuickExpense;
    private com.google.android.material.button.MaterialButton btnQuickStock, btnQuickCategory;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        
        // Get MainActivity reference
        mainActivity = (MainActivity) requireActivity();
        
        // Initialize views
        initViews(view);
        
        // Observe data
        observeViewModel();
        
        // Setup store selection for admin and load dashboard data
        setupStoreSelection();
        
        // Note: loadDashboardData() is now called in setupStoreSelection()
        if (loginViewModel.isAdmin()) {
            mainActivity.spinnerStore.setVisibility(View.VISIBLE);
            mainActivity.labelStore.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mainActivity != null) {
            mainActivity.spinnerStore.setVisibility(View.GONE);
            mainActivity.labelStore.setVisibility(View.GONE);
        }
    }
    
    private void initViews(View view) {
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvTodaySales = view.findViewById(R.id.tvTodaySales);
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts);
        tvLowStockCount = view.findViewById(R.id.tvLowStockCount);
        tvPendingTransactions = view.findViewById(R.id.tvPendingTransactions);
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses);
        tvProfitMargin = view.findViewById(R.id.tvProfitMargin);
        tvModalAwal = view.findViewById(R.id.tvModalAwal);
        modalAwalRepository = new ModalAwalRepository(requireActivity().getApplication());
        
        // Initialize quick action buttons
        btnQuickTransaction = view.findViewById(R.id.btn_quick_transaction);
        btnQuickProduct = view.findViewById(R.id.btn_quick_product);
        btnQuickExpense = view.findViewById(R.id.btn_quick_expense);
        btnQuickStock = view.findViewById(R.id.btn_quick_stock);
        btnQuickCategory = view.findViewById(R.id.btn_quick_category);
        
        // Setup quick action listeners
        setupQuickActions();
        
        // Jangan panggil tampilkanModalAwal() di sini, panggil di spinner
    }
    
    private void tampilkanModalAwal(int storeId) {
        Calendar cal = Calendar.getInstance();
        long hariIni = cal.get(Calendar.YEAR) * 10000 + (cal.get(Calendar.MONTH)+1) * 100 + cal.get(Calendar.DAY_OF_MONTH);
        final int finalStoreId = storeId;
        new Thread(() -> {
            ModalAwal modalAwal = modalAwalRepository.getModalAwalByTanggal(hariIni, finalStoreId);
            double nominal = modalAwal != null ? modalAwal.nominal : 0.0;

            // Check if fragment is still attached to activity before updating UI
            if (getActivity() != null && isAdded()) {
                getActivity().runOnUiThread(() -> {
                    // Double check fragment is still attached when UI thread runs
                    if (getActivity() != null && isAdded() && tvModalAwal != null) {
                        tvModalAwal.setText("Modal Awal: " + id.tugas.pos.utils.CurrencyUtils.formatCurrency(nominal));
                    }
                });
            }
        }).start();
    }
    
    private void setupStoreSelection() {
        // Check if user is admin
        if (loginViewModel.isAdmin()) {
            Log.d(TAG, "User is admin, setting up store selection");
            
            // Setup toolbar title and subtitle
            mainActivity.setToolbarTitle("Dashboard", "Semua Toko");
            
            // Setup store spinner in toolbar
            loginViewModel.getStores().observe(getViewLifecycleOwner(), stores -> {
                if (stores != null && !stores.isEmpty()) {
                    mainActivity.setupToolbarStoreSpinner(stores, new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Store selectedStore = (Store) parent.getItemAtPosition(position);
                            Log.d(TAG, "Store selected: " + selectedStore.getName() + " (ID: " + selectedStore.getId() + ")");
                            
                            // Update toolbar subtitle
                            mainActivity.setToolbarTitle("Dashboard", selectedStore.getName());
                            
                            // Jika pilih 'Semua Toko', load semua data
                            tampilkanModalAwal(selectedStore.getId());
                            if (selectedStore.getId() == -1) {
                                dashboardViewModel.loadDashboardData();
                            } else {
                                dashboardViewModel.loadDashboardDataByStore(selectedStore.getId());
                            }
                        }
                        
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            mainActivity.setToolbarTitle("Dashboard", "Semua Toko");
                            tampilkanModalAwal(-1);
                            dashboardViewModel.loadDashboardData();
                        }
                    });
                }
            });
        } else {
            Log.d(TAG, "User is not admin, using default store");
            
            // Get current user's store
            loginViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
                if (user != null && user.getStoreId() != null) {
                    Log.d(TAG, "setupStoreSelection: User storeId: " + user.getStoreId());
                    
                    // Get store name
                    loginViewModel.getStoreById(user.getStoreId()).observe(getViewLifecycleOwner(), store -> {
                        if (store != null) {
                            mainActivity.setToolbarTitle("Dashboard", store.getName());
                            Log.d(TAG, "setupStoreSelection: Store name: " + store.getName());
                            
                            // Load dashboard data for user's store
                            tampilkanModalAwal(user.getStoreId());
                            dashboardViewModel.loadDashboardDataByStore(user.getStoreId());
                        }
                    });
                } else {
                    Log.d(TAG, "setupStoreSelection: User has no storeId, loading all data");
                    // Fallback: load all data
                    tampilkanModalAwal(-1);
                    dashboardViewModel.loadDashboardData();
                }
            });
        }
    }
    
    private void loadDashboardData() {
        Log.d(TAG, "Loading dashboard data");
        dashboardViewModel.loadDashboardData();
    }
    
    private void observeViewModel() {
        // Observe total revenue
        dashboardViewModel.getTotalRevenue().observe(getViewLifecycleOwner(), revenue -> {
            Log.d(TAG, "observeViewModel: Total revenue updated: " + revenue);
            if (revenue != null) {
                tvTotalRevenue.setText(id.tugas.pos.utils.CurrencyUtils.formatCurrency(revenue));
            } else {
                tvTotalRevenue.setText(id.tugas.pos.utils.CurrencyUtils.formatCurrency(0.0));
            }
        });
        
        // Observe today's sales
        dashboardViewModel.getTodaySales().observe(getViewLifecycleOwner(), sales -> {
            Log.d(TAG, "observeViewModel: Today sales updated: " + sales);
            if (sales != null) {
                tvTodaySales.setText(id.tugas.pos.utils.CurrencyUtils.formatCurrency(sales));
            } else {
                tvTodaySales.setText(id.tugas.pos.utils.CurrencyUtils.formatCurrency(0.0));
            }
        });
        
        // Observe total products
        dashboardViewModel.getTotalProducts().observe(getViewLifecycleOwner(), count -> {
            Log.d(TAG, "observeViewModel: Total products updated: " + count);
            if (count != null) {
                tvTotalProducts.setText(String.valueOf(count));
            } else {
                tvTotalProducts.setText("0");
            }
        });
        
        // Observe low stock count
        dashboardViewModel.getLowStockCount().observe(getViewLifecycleOwner(), count -> {
            Log.d(TAG, "observeViewModel: Low stock count updated: " + count);
            if (count != null) {
                tvLowStockCount.setText(String.valueOf(count));
            } else {
                tvLowStockCount.setText("0");
            }
        });
        
        // Observe pending transactions
        dashboardViewModel.getPendingTransactions().observe(getViewLifecycleOwner(), count -> {
            Log.d(TAG, "observeViewModel: Pending transactions updated: " + count);
            if (count != null) {
                tvPendingTransactions.setText(String.valueOf(count));
            } else {
                tvPendingTransactions.setText("0");
            }
        });
        
        // Observe total expenses
        dashboardViewModel.getTotalExpenses().observe(getViewLifecycleOwner(), expenses -> {
            Log.d(TAG, "observeViewModel: Total expenses updated: " + expenses);
            if (expenses != null) {
                tvTotalExpenses.setText(id.tugas.pos.utils.CurrencyUtils.formatCurrency(expenses));
            } else {
                tvTotalExpenses.setText(id.tugas.pos.utils.CurrencyUtils.formatCurrency(0.0));
            }
        });
        
        // Observe profit margin
        dashboardViewModel.getProfitMargin().observe(getViewLifecycleOwner(), margin -> {
            Log.d(TAG, "observeViewModel: Profit margin updated: " + margin);
            if (margin != null) {
                tvProfitMargin.setText(id.tugas.pos.utils.CurrencyUtils.formatPercentage(margin));
            } else {
                tvProfitMargin.setText("0%");
            }
        });
    }
    
    // Method untuk force refresh dashboard data
    public void refreshDashboardData() {
        Log.d(TAG, "refreshDashboardData: Force refreshing dashboard data");
        dashboardViewModel.forceRefreshAllData();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        requireActivity().invalidateOptionsMenu();
        Log.d(TAG, "onResume: Fragment resumed");
        // Tidak perlu refresh data setiap kali resume
        // Data akan di-load otomatis saat fragment pertama kali dibuat
    }
    
    private void setupQuickActions() {
        // Quick Transaction
        btnQuickTransaction.setOnClickListener(v -> {
            mainActivity.loadFragment(new id.tugas.pos.ui.transaksi.TransaksiFragment());
            mainActivity.getSupportActionBar().setTitle("Transaksi");
        });
        
        // Quick Product
        btnQuickProduct.setOnClickListener(v -> {
            mainActivity.loadFragment(new id.tugas.pos.ui.produk.ProdukFragment());
            mainActivity.getSupportActionBar().setTitle("Produk");
        });
        
        // Quick Expense
        btnQuickExpense.setOnClickListener(v -> {
            mainActivity.loadFragment(new id.tugas.pos.ui.expense.ExpenseFragment());
            mainActivity.getSupportActionBar().setTitle("Pengeluaran");
        });
        
        // Quick Stock (same as product but focuses on inventory)
        btnQuickStock.setOnClickListener(v -> {
            mainActivity.loadFragment(new id.tugas.pos.ui.produk.ProdukFragment());
            mainActivity.getSupportActionBar().setTitle("Produk");
        });
        
        // Quick Category
        btnQuickCategory.setOnClickListener(v -> {
            showAddCategoryDialog();
        });
        

    }
    
    private void showAddCategoryDialog() {
        // Show AddCategoryDialog
        id.tugas.pos.ui.produk.dialog.AddCategoryDialog dialog = 
            id.tugas.pos.ui.produk.dialog.AddCategoryDialog.newInstance();
        dialog.setOnCategorySavedListener(categoryName -> {
            android.widget.Toast.makeText(requireContext(), 
                "Kategori '" + categoryName + "' berhasil ditambahkan", 
                android.widget.Toast.LENGTH_SHORT).show();
        });
        dialog.show(getChildFragmentManager(), "AddCategoryDialog");
    }
    

}
