package id.tugas.pos.ui.produk;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import id.tugas.pos.R;
import id.tugas.pos.data.model.Product;
import id.tugas.pos.data.model.Store;
import id.tugas.pos.ui.produk.adapter.ProductAdapter;
import id.tugas.pos.ui.produk.dialog.AddEditProductDialog;
import id.tugas.pos.utils.CurrencyUtils;
import id.tugas.pos.viewmodel.LoginViewModel;
import id.tugas.pos.viewmodel.StoreViewModel;

public class ProdukFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private ProdukViewModel viewModel;
    private ProductAdapter adapter;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private FloatingActionButton fabAdd;
    private List<Product> allProducts = new ArrayList<>();
    private LoginViewModel loginViewModel;
    private StoreViewModel storeViewModel;
    private Spinner spinnerStore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_produk, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupSearchView();
        setupFab();
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        storeViewModel = new ViewModelProvider(requireActivity()).get(StoreViewModel.class);
        setupStoreDropdown();
        observeData();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_products);
        searchView = view.findViewById(R.id.search_view_products);
        fabAdd = view.findViewById(R.id.fab_add_product);
        spinnerStore = view.findViewById(R.id.spinner_store); // Tambahkan spinner di layout
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProdukViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
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

    private void setupFab() {
        fabAdd.setOnClickListener(v -> showAddProductDialog());
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
                    // Admin: filter produk sesuai toko yang dipilih
                    storeViewModel.getSelectedStoreId().observe(getViewLifecycleOwner(), storeId -> {
                        if (storeId != null) {
                            viewModel.getAllProductsByStore(storeId).observe(getViewLifecycleOwner(), products -> {
                                allProducts = products;
                                adapter.submitList(products);
                            });
                        }
                    });
                } else {
                    // User: tampilkan produk sesuai toko
                    Integer storeId = user.getStoreId();
                    if (storeId != null) {
                        viewModel.getAllProductsByStore(storeId).observe(getViewLifecycleOwner(), products -> {
                            allProducts = products;
                            adapter.submitList(products);
                        });
                    }
                }
            }
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
        adapter.submitList(filteredList);
    }

    private void showAddProductDialog() {
        AddEditProductDialog dialog = AddEditProductDialog.newInstance(null);
        dialog.setOnProductSavedListener(product -> {
            viewModel.addProduct(product);
            Toast.makeText(requireContext(), "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getChildFragmentManager(), "AddProductDialog");
    }

    private void showEditProductDialog(Product product) {
        AddEditProductDialog dialog = AddEditProductDialog.newInstance(product);
        dialog.setOnProductSavedListener(updatedProduct -> {
            viewModel.updateProduct(updatedProduct);
            Toast.makeText(requireContext(), "Produk berhasil diperbarui", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getChildFragmentManager(), "EditProductDialog");
    }

    private void showDeleteConfirmationDialog(Product product) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus Produk")
                .setMessage("Apakah Anda yakin ingin menghapus produk \"" + product.getName() + "\"?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    viewModel.deleteProduct(product);
                    Toast.makeText(requireContext(), "Produk berhasil dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onProductClick(Product product) {
        showEditProductDialog(product);
    }

    @Override
    public void onProductLongClick(Product product) {
        showDeleteConfirmationDialog(product);
    }
} 