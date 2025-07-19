package id.tugas.pos.ui.transaksi;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import id.tugas.pos.R;
import id.tugas.pos.data.model.Product;
import id.tugas.pos.data.model.Transaction;
import id.tugas.pos.data.model.TransactionItem;
import id.tugas.pos.ui.transaksi.adapter.CartAdapter;
import id.tugas.pos.ui.transaksi.adapter.ProductGridAdapter;
import id.tugas.pos.utils.CurrencyUtils;
import id.tugas.pos.data.model.Store;
import id.tugas.pos.viewmodel.LoginViewModel;
import id.tugas.pos.viewmodel.StoreViewModel;
import id.tugas.pos.viewmodel.DashboardViewModel;
import id.tugas.pos.utils.PrinterUtils;

public class TransaksiFragment extends Fragment implements ProductGridAdapter.OnProductClickListener, CartAdapter.OnCartItemClickListener {

    private static final String TAG = "TransaksiFragment";
    private TransaksiViewModel viewModel;
    private ProductGridAdapter productAdapter;
    private CartAdapter cartAdapter;
    private RecyclerView recyclerViewProducts, recyclerViewCart;
    private SearchView searchView;
    private TextView tvTotalAmount, tvTotalItems;
    private MaterialButton btnCheckout, btnClearCart;
    private List<Product> allProducts = new ArrayList<>();
    private List<TransactionItem> cartItems = new ArrayList<>();
    private LoginViewModel loginViewModel;
    private StoreViewModel storeViewModel;
    private Spinner spinnerStore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaksi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupViewModel();
        setupRecyclerViews();
        setupSearchView();
        setupButtons();
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        storeViewModel = new ViewModelProvider(requireActivity()).get(StoreViewModel.class);
        setupStoreDropdown();
        observeData();
    }

    private void initViews(View view) {
        recyclerViewProducts = view.findViewById(R.id.recycler_view_products);
        recyclerViewCart = view.findViewById(R.id.recycler_view_cart);
        searchView = view.findViewById(R.id.search_view_products);
        tvTotalAmount = view.findViewById(R.id.tv_total_amount);
        tvTotalItems = view.findViewById(R.id.tv_total_items);
        btnCheckout = view.findViewById(R.id.btn_checkout);
        btnClearCart = view.findViewById(R.id.btn_clear_cart);
        spinnerStore = view.findViewById(R.id.spinner_store);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TransaksiViewModel.class);
    }

    private void setupRecyclerViews() {
        // Products grid
        productAdapter = new ProductGridAdapter(this);
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        recyclerViewProducts.setAdapter(productAdapter);

        // Cart list
        cartAdapter = new CartAdapter(this);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewCart.setAdapter(cartAdapter);
        
        // Debug: Check RecyclerView visibility and size
        recyclerViewCart.post(() -> {
            Log.d(TAG, "setupRecyclerViews: Cart RecyclerView width: " + recyclerViewCart.getWidth() + ", height: " + recyclerViewCart.getHeight());
            Log.d(TAG, "setupRecyclerViews: Cart RecyclerView visibility: " + (recyclerViewCart.getVisibility() == View.VISIBLE ? "VISIBLE" : "NOT VISIBLE"));
            Log.d(TAG, "setupRecyclerViews: Cart RecyclerView background: " + recyclerViewCart.getBackground());
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return true;
            }
        });
    }

    private void setupButtons() {
        btnCheckout.setOnClickListener(v -> showCheckoutDialog());
        btnClearCart.setOnClickListener(v -> showClearCartDialog());
    }

    private void setupStoreDropdown() {
        loginViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.isAdmin()) {
                spinnerStore.setVisibility(View.VISIBLE);
                storeViewModel.getAllStores().observe(getViewLifecycleOwner(), stores -> {
                    ArrayAdapter<Store> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, stores);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerStore.setAdapter(adapter);
                    spinnerStore.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                            Store selected = stores.get(position);
                            storeViewModel.setSelectedStoreId(selected.getId());
                        }
                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                    });
                });
            } else {
                spinnerStore.setVisibility(View.GONE);
            }
        });
    }

    private void observeData() {
        Log.d(TAG, "observeData: Starting to observe data");
        
        loginViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            Log.d(TAG, "observeData: User observed: " + (user != null ? user.getUsername() : "null"));
            if (user != null) {
                if (user.isAdmin()) {
                    Log.d(TAG, "observeData: User is admin, setting up store selection");
                    storeViewModel.getSelectedStoreId().observe(getViewLifecycleOwner(), storeId -> {
                        Log.d(TAG, "observeData: Admin storeId: " + storeId);
                        if (storeId != null) {
                            viewModel.getAllProductsByStore(storeId).observe(getViewLifecycleOwner(), products -> {
                                Log.d(TAG, "observeData: Admin products loaded: " + (products != null ? products.size() : 0));
                                allProducts = products;
                                productAdapter.submitList(products);
                            });
                        }
                    });
                } else {
                    Integer storeId = user.getStoreId();
                    Log.d(TAG, "observeData: User storeId: " + storeId);
                    if (storeId != null) {
                        viewModel.getAllProductsByStore(storeId).observe(getViewLifecycleOwner(), products -> {
                            Log.d(TAG, "observeData: User products loaded: " + (products != null ? products.size() : 0));
                            allProducts = products;
                            productAdapter.submitList(products);
                        });
                    }
                }
            }
        });

        viewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            Log.d(TAG, "observeData: Cart items observed: " + (items != null ? items.size() : 0));
            cartItems = items;
            
            // Debug: Log detail setiap item
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    TransactionItem item = items.get(i);
                    Log.d(TAG, "observeData: Cart item " + i + ": ID=" + item.getProductId() + ", Name=" + item.getName() + ", Qty=" + item.getQuantity() + ", Price=" + item.getPrice());
                }
            }
            
            Log.d(TAG, "observeData: Submitting list to adapter with " + (items != null ? items.size() : 0) + " items");
            cartAdapter.submitList(items);
            
            // Debug: Check adapter state after submit
            recyclerViewCart.post(() -> {
                Log.d(TAG, "observeData: After submitList - Adapter item count: " + cartAdapter.getItemCount());
                Log.d(TAG, "observeData: After submitList - RecyclerView child count: " + recyclerViewCart.getChildCount());
                Log.d(TAG, "observeData: After submitList - RecyclerView layout manager: " + recyclerViewCart.getLayoutManager());
            });
            
            updateCartSummary();
        });
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : allProducts) {
            if (product.getName().toLowerCase().contains(query.toLowerCase()) ||
                product.getCode().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productAdapter.submitList(filteredList);
    }

    private void updateCartSummary() {
        Log.d(TAG, "updateCartSummary: Updating cart summary with " + cartItems.size() + " items");
        int totalItems = 0;
        double totalAmount = 0;

        for (TransactionItem item : cartItems) {
            totalItems += item.getQuantity();
            totalAmount += item.getSubtotal();
        }

        Log.d(TAG, "updateCartSummary: Total items: " + totalItems + ", Total amount: " + totalAmount);
        tvTotalItems.setText("Total Item: " + totalItems);
        tvTotalAmount.setText(CurrencyUtils.formatCurrency(totalAmount));
    }

    private void showCheckoutDialog() {
        if (cartItems.isEmpty()) {
            Toast.makeText(requireContext(), "Keranjang kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        final double totalAmount = cartItems.stream()
                .mapToDouble(TransactionItem::getSubtotal)
                .sum();

        // Create custom dialog layout
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_checkout, null);
        TextView tvTotalAmount = dialogView.findViewById(R.id.tv_total_amount);
        EditText etAmountPaid = dialogView.findViewById(R.id.et_amount_paid);
        TextView tvChange = dialogView.findViewById(R.id.tv_change);
        TextView tvChangeLabel = dialogView.findViewById(R.id.tv_change_label);

        tvTotalAmount.setText(CurrencyUtils.formatCurrency(totalAmount));
        etAmountPaid.setText(String.valueOf((int) totalAmount)); // Set default value
        etAmountPaid.selectAll(); // Select all text for easy editing

        // Calculate change when amount paid changes
        etAmountPaid.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                try {
                    double amountPaid = Double.parseDouble(s.toString());
                    double change = amountPaid - totalAmount;
                    
                    if (change >= 0) {
                        tvChange.setText(CurrencyUtils.formatCurrency(change));
                        tvChange.setTextColor(requireContext().getResources().getColor(android.R.color.holo_green_dark));
                        tvChangeLabel.setText("Kembalian:");
                    } else {
                        tvChange.setText(CurrencyUtils.formatCurrency(Math.abs(change)));
                        tvChange.setTextColor(requireContext().getResources().getColor(android.R.color.holo_red_dark));
                        tvChangeLabel.setText("Kurang:");
                    }
                } catch (NumberFormatException e) {
                    tvChange.setText("Rp 0");
                    tvChangeLabel.setText("Kembalian:");
                }
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Pembayaran")
                .setView(dialogView)
                .setPositiveButton("Bayar", null) // Set to null first, we'll override it
                .setNegativeButton("Batal", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                try {
                    double amountPaid = Double.parseDouble(etAmountPaid.getText().toString());
                    
                    if (amountPaid < totalAmount) {
                        Toast.makeText(requireContext(), "Jumlah pembayaran kurang dari total", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Process transaction with payment amount
                    viewModel.processTransaction(cartItems, totalAmount, amountPaid);
                    
                    // Show success dialog with change
                    double change = amountPaid - totalAmount;
                    showSuccessDialog(totalAmount, amountPaid, change);
                    
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Masukkan jumlah pembayaran yang valid", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showSuccessDialog(double totalAmount, double amountPaid, double change) {
        String message = "Transaksi Berhasil!\n\n" +
                "Total: " + CurrencyUtils.formatCurrency(totalAmount) + "\n" +
                "Dibayar: " + CurrencyUtils.formatCurrency(amountPaid) + "\n" +
                "Kembalian: " + CurrencyUtils.formatCurrency(change);

        new AlertDialog.Builder(requireContext())
                .setTitle("Sukses")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Cart will be cleared automatically by ViewModel
                    // Refresh dashboard data
                    refreshDashboardData();
                    // Notifikasi print
                    if (PrinterUtils.isPrinterConnected()) {
                        Toast.makeText(requireContext(), "Struk berhasil dikirim ke printer", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Struk GAGAL dikirim ke printer! Pastikan printer aktif & terhubung", Toast.LENGTH_LONG).show();
                    }
                })
                .setCancelable(false)
                .show();
    }
    
    private void refreshDashboardData() {
        Log.d(TAG, "refreshDashboardData: Refreshing dashboard data");
        // Get DashboardViewModel and refresh data
        try {
            DashboardViewModel dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
            // Force refresh all dashboard data
            dashboardViewModel.forceRefreshAllData();
            Log.d(TAG, "refreshDashboardData: Dashboard force refresh triggered");
        } catch (Exception e) {
            Log.e(TAG, "refreshDashboardData: Error refreshing dashboard", e);
        }
    }

    private void showClearCartDialog() {
        if (cartItems.isEmpty()) {
            Toast.makeText(requireContext(), "Keranjang sudah kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Kosongkan Keranjang")
                .setMessage("Apakah Anda yakin ingin mengosongkan keranjang?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    viewModel.clearCart();
                    Toast.makeText(requireContext(), "Keranjang dikosongkan", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onProductClick(Product product) {
        Log.d(TAG, "onProductClick: Adding product to cart: " + product.getName());
        viewModel.addToCart(product);
        Toast.makeText(requireContext(), product.getName() + " ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCartItemClick(TransactionItem item) {
        // Show quantity adjustment dialog
        showQuantityDialog(item);
    }

    @Override
    public void onCartItemLongClick(TransactionItem item) {
        // Remove item from cart
        viewModel.removeFromCart(item);
        Toast.makeText(requireContext(), "Item dihapus dari keranjang", Toast.LENGTH_SHORT).show();
    }

    private void showQuantityDialog(TransactionItem item) {
        String[] quantities = {"1", "2", "3", "4", "5", "10"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Atur Jumlah")
                .setItems(quantities, (dialog, which) -> {
                    int quantity = Integer.parseInt(quantities[which]);
                    viewModel.updateCartItemQuantity(item, quantity);
                })
                .setNegativeButton("Batal", null)
                .show();
    }
} 