package id.tugas.pos.ui.transaksi;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

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

public class TransaksiFragment extends Fragment implements ProductGridAdapter.OnProductClickListener, CartAdapter.OnCartItemClickListener {

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
        loginViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                if (user.isAdmin()) {
                    storeViewModel.getSelectedStoreId().observe(getViewLifecycleOwner(), storeId -> {
                        if (storeId != null) {
                            viewModel.getAllProductsByStore(storeId).observe(getViewLifecycleOwner(), products -> {
                                allProducts = products;
                                productAdapter.submitList(products);
                            });
                            viewModel.getAllTransactionsByStore(storeId).observe(getViewLifecycleOwner(), transactions -> {
                                // update transaksi list jika ada
                            });
                        }
                    });
                } else {
                    Integer storeId = user.getStoreId();
                    if (storeId != null) {
                        viewModel.getAllProductsByStore(storeId).observe(getViewLifecycleOwner(), products -> {
                            allProducts = products;
                            productAdapter.submitList(products);
                        });
                        viewModel.getAllTransactionsByStore(storeId).observe(getViewLifecycleOwner(), transactions -> {
                            // update transaksi list jika ada
                        });
                    }
                }
            }
        });

        viewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            cartItems = items;
            cartAdapter.submitList(items);
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
        int totalItems = 0;
        double totalAmount = 0;

        for (TransactionItem item : cartItems) {
            totalItems += item.getQuantity();
            totalAmount += item.getSubtotal();
        }

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

        new AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Transaksi")
                .setMessage("Total: " + CurrencyUtils.formatCurrency(totalAmount) + "\n\nLanjutkan transaksi?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    viewModel.processTransaction(cartItems, totalAmount);
                    Toast.makeText(requireContext(), "Transaksi berhasil", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
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