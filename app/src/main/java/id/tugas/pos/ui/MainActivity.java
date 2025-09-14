package id.tugas.pos.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import id.tugas.pos.R;
import id.tugas.pos.data.model.User;
import id.tugas.pos.ui.dashboard.DashboardFragment;
import id.tugas.pos.ui.expense.ExpenseFragment;
import id.tugas.pos.ui.history.HistoryFragment;
import id.tugas.pos.ui.login.LoginActivity;
import id.tugas.pos.ui.produk.ProdukFragment;
import id.tugas.pos.ui.report.ReportFragment;
import id.tugas.pos.ui.settings.SettingsFragment;
import id.tugas.pos.ui.transaksi.TransaksiFragment;
import id.tugas.pos.ui.user.UserManagementFragment;
import id.tugas.pos.viewmodel.DashboardViewModel;
import id.tugas.pos.viewmodel.LoginViewModel;
import android.util.Log;
import android.widget.Spinner; // Tambahkan import untuk Spinner
import id.tugas.pos.data.model.Store;
import id.tugas.pos.viewmodel.ProductViewModel;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView tvUserName, tvUserRole;
    private LoginViewModel loginViewModel;
    private User currentUser;
    public Spinner spinnerStore;
    public TextView labelStore;
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabQuickActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set full screen flags - hide all system UI
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN |
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN |
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        );
        
        // For Android 9+ (API 28+), use edge-to-edge display
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = 
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        
        // Hide system UI completely
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // For Android 11+ (API 30+)
            getWindow().getDecorView().getWindowInsetsController().hide(
                android.view.WindowInsets.Type.statusBars() | 
                android.view.WindowInsets.Type.navigationBars()
            );
        } else {
            // For older Android versions
            getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN |
                android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
        
        setContentView(R.layout.activity_main);
        
        // Initialize ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        
        // Observe currentUser and jump to login if null
        loginViewModel.getCurrentUser().observe(this, user -> {
            Log.d(TAG, "MainActivity: currentUser changed: " + new Gson().toJson(user));
            if (user == null) {
                Log.d(TAG, "MainActivity: currentUser is null, navigating to login");
                navigateToLogin();
            } else {
                Log.d(TAG, "MainActivity: currentUser is not null, setting up user info");
                currentUser = user;
                setupUserInfo();
                // Force reload dashboard and product data for new user
                DashboardViewModel dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
                dashboardViewModel.loadDashboardData();
                ProductViewModel productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
                if (currentUser != null && currentUser.getStoreId() != null) {
                    productViewModel.setStoreId(currentUser.getStoreId());
                }
                // Load default fragment setelah currentUser tersedia
                if (savedInstanceState == null) {
                    // Load default fragment based on user role
                    if (loginViewModel.isAdmin()) {
                        Log.d(TAG, "MainActivity: User is admin, loading Dashboard");
                        loadFragment(new DashboardFragment());
                        getSupportActionBar().setTitle("Dashboard");
                    } else {
                        Log.d(TAG, "MainActivity: User is not admin, loading Transaksi");
                        loadFragment(new TransaksiFragment());
                        getSupportActionBar().setTitle("Transaksi");
                    }
                }
            }
        });
        
        // Check if user is logged in (pakai SharedPreferences)
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            navigateToLogin();
            return;
        }
        
        // Initialize views
        initViews();
        setupToolbar();
        setupNavigation();
        setupBottomNavigation();
        setupQuickActions();
        
        // Show store spinner by default, only hide for non-admin users
        if (loginViewModel.isAdmin()) {
            spinnerStore.setVisibility(View.VISIBLE);
            labelStore.setVisibility(View.VISIBLE);
        } else {
            spinnerStore.setVisibility(View.GONE);
            labelStore.setVisibility(View.GONE);
        }

        // HAPUS: Logic default fragment dari sini karena sudah dipindah ke observer
    }
    
    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fabQuickActions = findViewById(R.id.fabQuickActions);
        
        // Get header views
        View headerView = navigationView.getHeaderView(0);
        tvUserName = headerView.findViewById(R.id.tvUserName);
        tvUserRole = headerView.findViewById(R.id.tvUserRole);
        
        // Inisialisasi spinnerStore dan labelStore dari toolbar
        spinnerStore = toolbar.findViewById(R.id.spinnerStore);
        labelStore = toolbar.findViewById(R.id.labelStore);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dashboard");
        getSupportActionBar().setSubtitle(null); // Clear subtitle
        
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }
    

    
    // Method untuk mengatur title dan subtitle dari fragment
    public void setToolbarTitle(String title, String subtitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setSubtitle(subtitle);
        }
    }
    
    // Method untuk setup spinner toolbar
    public void setupToolbarStoreSpinner(
            java.util.List<Store> stores,
            android.widget.AdapterView.OnItemSelectedListener listener) {
        if (spinnerStore != null && stores != null && !stores.isEmpty()) {
            android.widget.ArrayAdapter<Store> adapter =
                new android.widget.ArrayAdapter<>(this, R.layout.spinner_item_black_text, stores);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_black_text);
            spinnerStore.setAdapter(adapter);
            spinnerStore.setOnItemSelectedListener(listener);
        }
    }
    
    private void setupNavigation() {
        navigationView.setNavigationItemSelectedListener(this);
        
        // Navigasi hanya lewat navigationView (drawer)
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_dashboard) {
                if (loginViewModel.isAdmin()) {
                    loadFragment(new DashboardFragment());
                    getSupportActionBar().setTitle("Dashboard");
                }
            } else if (itemId == R.id.nav_products) {
                loadFragment(new ProdukFragment());
                getSupportActionBar().setTitle("Produk");
            } else if (itemId == R.id.nav_transaction) {
                loadFragment(new TransaksiFragment());
                getSupportActionBar().setTitle("Transaksi");
            } else if (itemId == R.id.nav_history) {
                loadFragment(new HistoryFragment());
                getSupportActionBar().setTitle("Riwayat");
            } else if (itemId == R.id.nav_expense) {
                loadFragment(new ExpenseFragment());
                getSupportActionBar().setTitle("Pengeluaran");
            }
            
            // Close drawer if open
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            
            return true;
        });
    }
    
    private void setupQuickActions() {
        fabQuickActions.setOnClickListener(v -> showQuickActionsDialog());
    }
    
    private void showQuickActionsDialog() {
        String[] actions = {
            "Tambah Produk Baru",
            "Tambah Kategori",
            "Tambah Pengeluaran",
            "Tambah Stok Masuk",
            "Cek Stok"
        };
        
        new AlertDialog.Builder(this)
            .setTitle("Aksi Cepat")
            .setItems(actions, (dialog, which) -> {
                switch (which) {
                    case 0: // Tambah Produk Baru
                        loadFragment(new ProdukFragment());
                        getSupportActionBar().setTitle("Produk");
                        break;
                    case 1: // Tambah Kategori
                        showAddCategoryDialog();
                        break;
                    case 2: // Tambah Pengeluaran
                        loadFragment(new ExpenseFragment());
                        getSupportActionBar().setTitle("Pengeluaran Kas Kecil");
                        break;
                    case 3: // Tambah Stok Masuk
                        showStockInDialog();
                        break;
                    case 4: // Cek Stok
                        showStockCheckDialog();
                        break;
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }
    
    private void showAddCategoryDialog() {
        // Show AddCategoryDialog
        id.tugas.pos.ui.produk.dialog.AddCategoryDialog dialog = 
            id.tugas.pos.ui.produk.dialog.AddCategoryDialog.newInstance();
        dialog.setOnCategorySavedListener(categoryName -> {
            Toast.makeText(this, "Kategori '" + categoryName + "' berhasil ditambahkan", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getSupportFragmentManager(), "AddCategoryDialog");
    }
    
    private void showStockInDialog() {
        // Show StockInDialog
        id.tugas.pos.ui.stockin.StockInDialogFragment dialog = 
            new id.tugas.pos.ui.stockin.StockInDialogFragment();
        dialog.show(getSupportFragmentManager(), "StockInDialog");
    }
    

    
    private void showStockCheckDialog() {
        // Show stock check dialog
        new AlertDialog.Builder(this)
            .setTitle("Cek Stok")
            .setMessage("Fitur ini akan menampilkan produk dengan stok rendah atau habis")
            .setPositiveButton("Lihat", (dialog, which) -> {
                loadFragment(new ProdukFragment());
                getSupportActionBar().setTitle("Produk");
            })
            .setNegativeButton("Batal", null)
            .show();
    }
    
    private void setupUserInfo() {
        String userName = currentUser != null ? currentUser.getEmail() : "Guest";
        String userRole = (currentUser != null ? currentUser.getRole().equalsIgnoreCase("admin") : false) ? "Administrator" : "Cashier";
        
        tvUserName.setText(userName);
        tvUserRole.setText(userRole);
        
        Menu menu = navigationView.getMenu();
        if (!loginViewModel.isAdmin()) {
            MenuItem item;
            item = menu.findItem(R.id.nav_users); if (item != null) item.setVisible(false);
            item = menu.findItem(R.id.nav_settings); if (item != null) item.setVisible(false);
            item = menu.findItem(R.id.nav_dashboard); if (item != null) item.setVisible(false); // Hide dashboard for users
            // item = menu.findItem(R.id.nav_expense); if (item != null) item.setVisible(false); // Pengeluaran DITAMPILKAN untuk user
            item = menu.findItem(R.id.nav_report); if (item != null) item.setVisible(false); // Hide report for users
        } else {
            // Hide transaksi, riwayat, dan pengeluaran untuk admin
            MenuItem item;
            item = menu.findItem(R.id.nav_transaction); if (item != null) item.setVisible(false);
            item = menu.findItem(R.id.nav_history); if (item != null) item.setVisible(false);
            item = menu.findItem(R.id.nav_expense); if (item != null) item.setVisible(false);
        }
        
        // Hide juga di bottom navigation
        if (loginViewModel.isAdmin()) {
            MenuItem item;
            item = menu.findItem(R.id.nav_transaction); if (item != null) item.setVisible(false);
            item = menu.findItem(R.id.nav_history); if (item != null) item.setVisible(false);
            item = menu.findItem(R.id.nav_expense); if (item != null) item.setVisible(false);
        } else {
            MenuItem item;
            item = menu.findItem(R.id.nav_dashboard); if (item != null) item.setVisible(false);
            item = menu.findItem(R.id.nav_report); if (item != null) item.setVisible(false);
            // Pengeluaran DITAMPILKAN untuk user
        }
        
        // Set spinner visibility based on user role
        if (loginViewModel.isAdmin()) {
            spinnerStore.setVisibility(View.VISIBLE);
            labelStore.setVisibility(View.VISIBLE);
        } else {
            spinnerStore.setVisibility(View.GONE);
            labelStore.setVisibility(View.GONE);
        }
        
        // Invalidate options menu to update toolbar items
        invalidateOptionsMenu();
    }
    
    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        
        // Clear subtitle dan spinner toolbar secara default
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(null);
        }
        // clearToolbarStoreSpinner(); // Hapus karena toolbarStoreSpinner sudah dihapus
        
        // Close drawer if open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_dashboard) {
            // Only allow dashboard for admin
            if (loginViewModel.isAdmin()) {
                loadFragment(new DashboardFragment());
                getSupportActionBar().setTitle("Dashboard");
                getSupportActionBar().setSubtitle(null);
            }
        } else if (itemId == R.id.nav_products) {
            loadFragment(new ProdukFragment());
            getSupportActionBar().setTitle("Produk");
            getSupportActionBar().setSubtitle(null);
        } else if (itemId == R.id.nav_transaction) {
            loadFragment(new TransaksiFragment());
            getSupportActionBar().setTitle("Transaksi");
            getSupportActionBar().setSubtitle(null);
        } else if (itemId == R.id.nav_history) {
            loadFragment(new HistoryFragment());
            getSupportActionBar().setTitle("Riwayat");
            getSupportActionBar().setSubtitle(null);
        } else if (itemId == R.id.nav_expense) {
            // Only allow expense for admin
                loadFragment(new ExpenseFragment());
                getSupportActionBar().setTitle("Pengeluaran");
                getSupportActionBar().setSubtitle(null);
        } else if (itemId == R.id.nav_report) {
            // Only allow report for admin
            if (loginViewModel.isAdmin()) {
                loadFragment(new ReportFragment());
                getSupportActionBar().setTitle("Laporan");
                getSupportActionBar().setSubtitle(null);
            }
        } else if (itemId == R.id.nav_users) {
            // Only allow user management for admin
            if (loginViewModel.isAdmin()) {
                loadFragment(new UserManagementFragment());
                getSupportActionBar().setTitle("Manajemen User");
                getSupportActionBar().setSubtitle(null);
            }
        } else if (itemId == R.id.nav_settings) {
            // Only allow settings for admin
            if (loginViewModel.isAdmin()) {
                loadFragment(new SettingsFragment());
                getSupportActionBar().setTitle("Pengaturan");
                getSupportActionBar().setSubtitle(null);
            }
        } else if (itemId == R.id.nav_modal_awal) {
            // Modal awal navigation - available for all users
            loadFragment(new id.tugas.pos.ui.modal.ModalAwalFragment());
            getSupportActionBar().setTitle("Modal Awal");
            getSupportActionBar().setSubtitle(null);
        } else if (itemId == R.id.nav_logout) {
            logout();
        }
        
        return true;
    }
    
    private void logout() {
        loginViewModel.logout();
        // Clear ViewModel data to prevent stale data after logout
        DashboardViewModel dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        dashboardViewModel.clearData();
        ProductViewModel productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        productViewModel.clearData();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Ensure system UI stays hidden
        hideSystemUI();
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
    
    private void hideSystemUI() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // For Android 11+ (API 30+)
            getWindow().getDecorView().getWindowInsetsController().hide(
                android.view.WindowInsets.Type.statusBars() | 
                android.view.WindowInsets.Type.navigationBars()
            );
        } else {
            // For older Android versions
            getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN |
                android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }
    
    // HAPUS: onCreateOptionsMenu method
}
