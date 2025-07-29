package id.tugas.pos.ui.produk;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.List;

import id.tugas.pos.R;
import id.tugas.pos.data.model.Product;
import id.tugas.pos.data.model.Store;
import id.tugas.pos.ui.produk.adapter.ProductAdapter;
import id.tugas.pos.ui.produk.dialog.AddEditProductDialog;
import id.tugas.pos.ui.produk.dialog.AddCategoryDialog;
import id.tugas.pos.utils.CurrencyUtils;
import id.tugas.pos.viewmodel.LoginViewModel;
import id.tugas.pos.viewmodel.StoreViewModel;
import id.tugas.pos.ui.MainActivity;

public class ProdukFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private ProdukViewModel viewModel;
    private ProductAdapter adapter;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private FloatingActionButton fabAdd;
    private View expandableMenu;
    private FloatingActionButton fabAddProductMenu;
    private FloatingActionButton fabAddCategoryMenu;
    private TextView labelAddProduct;
    private TextView labelAddCategory;
    private List<Product> allProducts = new ArrayList<>();
    private LoginViewModel loginViewModel;
    private StoreViewModel storeViewModel;
    private AlertDialog currentDialog; // Tambahkan field untuk menyimpan dialog
    private MainActivity mainActivity;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_produk, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity) requireActivity();
        
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupSearchView();
        setupFab();
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        storeViewModel = new ViewModelProvider(requireActivity()).get(StoreViewModel.class);
        setupStoreDropdown();
        observeData();
        // Show store spinner by default, only hide for non-admin users
        if (!loginViewModel.isAdmin()) {
            mainActivity.spinnerStore.setVisibility(View.GONE);
            mainActivity.labelStore.setVisibility(View.GONE);
        }
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_products);
        searchView = view.findViewById(R.id.search_view_products);
        fabAdd = view.findViewById(R.id.fab_add_product);
        expandableMenu = view.findViewById(R.id.expandable_menu);
        fabAddProductMenu = view.findViewById(R.id.fab_add_product_menu);
        fabAddCategoryMenu = view.findViewById(R.id.fab_add_category_menu);
        labelAddProduct = view.findViewById(R.id.label_add_product);
        labelAddCategory = view.findViewById(R.id.label_add_category);
        
        // Null check untuk recyclerView
        if (recyclerView == null) {
            // Log warning jika recyclerView tidak ditemukan
            android.util.Log.w("ProdukFragment", "recycler_view_products not found in layout");
        }
        
        // Null check untuk searchView
        if (searchView == null) {
            // Log warning jika searchView tidak ditemukan
            android.util.Log.w("ProdukFragment", "search_view_products not found in layout");
        }
        
        // Null check untuk fabAdd
        if (fabAdd == null) {
            // Log warning jika fabAdd tidak ditemukan
            android.util.Log.w("ProdukFragment", "fab_add_product not found in layout");
        }
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
        // Setup main FAB untuk toggle ArcMenu
        fabAdd.setOnClickListener(v -> toggleArcMenu());
        
        // Setup menu items
        fabAddProductMenu.setOnClickListener(v -> {
            // Collapse menu
            expandableMenu.setVisibility(View.GONE);
            labelAddProduct.setVisibility(View.GONE);
            labelAddCategory.setVisibility(View.GONE);
            showAddProductDialog();
        });
        
        fabAddCategoryMenu.setOnClickListener(v -> {
            // Collapse menu
            expandableMenu.setVisibility(View.GONE);
            labelAddProduct.setVisibility(View.GONE);
            labelAddCategory.setVisibility(View.GONE);
            showAddCategoryDialog();
        });
    }
    
    private void toggleArcMenu() {
        if (expandableMenu.getVisibility() == View.VISIBLE) {
            // Collapse menu
            Animation slideUp = AnimationUtils.loadAnimation(requireContext(), android.R.anim.slide_out_right);
            expandableMenu.startAnimation(slideUp);
            expandableMenu.setVisibility(View.GONE);
            
            // Hide labels
            labelAddProduct.setVisibility(View.GONE);
            labelAddCategory.setVisibility(View.GONE);
        } else {
            // Expand menu
            expandableMenu.setVisibility(View.VISIBLE);
            // Use fade in animation for smoother effect
            Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in);
            expandableMenu.startAnimation(fadeIn);
            
            // Show labels with delay for better UX
            labelAddProduct.postDelayed(() -> labelAddProduct.setVisibility(View.VISIBLE), 150);
            labelAddCategory.postDelayed(() -> labelAddCategory.setVisibility(View.VISIBLE), 250);
        }
    }

    private void setupStoreDropdown() {
        // Set toolbar title
        updateToolbarTitle("Manajemen Produk", null);
        
        loginViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.isAdmin()) {
                // Admin: gunakan spinner toolbar dengan pilihan toko tertentu (TIDAK ADA "Semua Toko")
                loginViewModel.getStoresForProduct().observe(getViewLifecycleOwner(), stores -> {
                    if (stores != null && !stores.isEmpty()) {
                        // Setup spinner toolbar
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).setupToolbarStoreSpinner(stores, 
                                new android.widget.AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                                        id.tugas.pos.data.model.Store selected = stores.get(position);
                                        storeViewModel.setSelectedStoreId(selected.getId());
                                        
                                        // Update toolbar subtitle dengan store yang dipilih
                                        updateToolbarTitle("Manajemen Produk", selected.getName());
                                        
                                        // Log untuk debug
                                        android.util.Log.d("ProdukFragment", "Admin selected store: " + selected.getName() + " (ID: " + selected.getId() + ")");
                                    }
                                    @Override
                                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                                        // Jika tidak ada yang dipilih, pilih store pertama
                                        if (stores.size() > 0) {
                                            storeViewModel.setSelectedStoreId(stores.get(0).getId());
                                            updateToolbarTitle("Manajemen Produk", stores.get(0).getName());
                                        }
                                    }
                                });
                            
                            // Set default selection ke store pertama (WAJIB pilih toko)
                            if (stores.size() > 0) {
                                storeViewModel.setSelectedStoreId(stores.get(0).getId());
                                updateToolbarTitle("Manajemen Produk", stores.get(0).getName());
                                android.util.Log.d("ProdukFragment", "Admin default store: " + stores.get(0).getName() + " (ID: " + stores.get(0).getId() + ")");
                            }
                        }
                    }
                });
            } else {
                // User: langsung gunakan store yang sudah ditetapkan
                if (user != null && user.getStoreId() != null) {
                    storeViewModel.setSelectedStoreId(user.getStoreId());
                    
                    // Tampilkan informasi store yang sedang aktif di toolbar
                    storeViewModel.getAllStores().observe(getViewLifecycleOwner(), stores -> {
                        if (stores != null) {
                            for (id.tugas.pos.data.model.Store store : stores) {
                                if (store.getId() == user.getStoreId()) {
                                    updateToolbarTitle("Manajemen Produk", store.getName());
                                    break;
                                }
                            }
                        }
                    });
                }
            }
        });
    }
    
    private void updateToolbarTitle(String title, String subtitle) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbarTitle(title, subtitle);
        }
    }

    private void observeData() {
        // Observe selected store ID (untuk admin) atau user's store ID (untuk user)
        storeViewModel.getSelectedStoreId().observe(getViewLifecycleOwner(), storeId -> {
            android.util.Log.d("ProdukFragment", "Selected storeId: " + storeId);
            if (storeId != null) {
                // Debug: cek semua produk tanpa filter storeId
                viewModel.getAllProducts().observe(getViewLifecycleOwner(), allProductsDebug -> {
                    android.util.Log.d("ProdukFragment", "Total produk di database: " + (allProductsDebug != null ? allProductsDebug.size() : 0));
                    if (allProductsDebug != null && !allProductsDebug.isEmpty()) {
                        for (id.tugas.pos.data.model.Product product : allProductsDebug) {
                            android.util.Log.d("ProdukFragment", "Produk: " + product.getName() + " (ID: " + product.getId() + ", StoreID: " + product.getStoreId() + ")");
                        }
                    }
                });
                
                viewModel.getAllProductsByStore(storeId).observe(getViewLifecycleOwner(), products -> {
                    android.util.Log.d("ProdukFragment", "Jumlah produk untuk storeId " + storeId + ": " + (products != null ? products.size() : 0));
                    allProducts = products;
                    adapter.submitList(products);
                });
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
        // Get current storeId
        Integer currentStoreId = storeViewModel.getSelectedStoreId().getValue();
        android.util.Log.d("ProdukFragment", "Creating dialog with storeId: " + currentStoreId);
        
        AddEditProductDialog dialog = AddEditProductDialog.newInstance(null, currentStoreId);
        dialog.setOnProductSavedListener(product -> {
            viewModel.addProduct(product);
            Toast.makeText(requireContext(), "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getChildFragmentManager(), "AddProductDialog");
    }
    
    private void showAddCategoryDialog() {
        AddCategoryDialog dialog = AddCategoryDialog.newInstance();
        dialog.setOnCategorySavedListener(categoryName -> {
            // For now, just show a toast message
            // In the future, this could be extended to save categories to a database
            Toast.makeText(requireContext(), "Kategori '" + categoryName + "' berhasil ditambahkan", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getChildFragmentManager(), "AddCategoryDialog");
    }

    private void showEditProductDialog(Product product) {
        // Get current storeId
        Integer currentStoreId = storeViewModel.getSelectedStoreId().getValue();
        
        AddEditProductDialog dialog = AddEditProductDialog.newInstance(product, currentStoreId);
        dialog.setOnProductSavedListener(updatedProduct -> {
            viewModel.updateProduct(updatedProduct);
            Toast.makeText(requireContext(), "Produk berhasil diperbarui", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getChildFragmentManager(), "EditProductDialog");
    }

    private void showDeleteConfirmationDialog(Product product) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                .setTitle("Hapus Produk")
                .setMessage("Apakah Anda yakin ingin menghapus produk \"" + product.getName() + "\"?")
                .setPositiveButton("Hapus", (dialogInterface, which) -> {
                    viewModel.deleteProduct(product);
                    Toast.makeText(requireContext(), "Produk berhasil dihapus", Toast.LENGTH_SHORT).show();
                    currentDialog = null;
                })
                .setNegativeButton("Batal", (dialogInterface, which) -> {
                    currentDialog = null;
                })
                .create();
        
        // Simpan dialog untuk cleanup
        currentDialog = dialog;
        dialog.show();
    }

    @Override
    public void onProductClick(Product product) {
        showEditProductDialog(product);
    }

    @Override
    public void onProductLongClick(Product product) {
        showDeleteConfirmationDialog(product);
    }
    
    @Override
    public void onEditClick(Product product) {
        showEditProductDialog(product);
    }
    
    @Override
    public void onDeleteClick(Product product) {
        showDeleteConfirmationDialog(product);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Reset toolbar title
        updateToolbarTitle("Produk", null);
        // Don't hide spinner toolbar here - let it remain visible
        // Dismiss dialog jika masih terbuka untuk mencegah window leak
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Dismiss dialog jika masih terbuka untuk mencegah window leak
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }
    }
} 