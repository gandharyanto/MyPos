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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import id.tugas.pos.R;
import id.tugas.pos.ui.dashboard.DashboardFragment;
import id.tugas.pos.ui.expense.ExpenseFragment;
import id.tugas.pos.ui.history.HistoryFragment;
import id.tugas.pos.ui.produk.ProdukFragment;
import id.tugas.pos.ui.report.ReportFragment;
import id.tugas.pos.ui.settings.SettingsFragment;
import id.tugas.pos.ui.transaksi.TransaksiFragment;
import id.tugas.pos.viewmodel.LoginViewModel;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private TextView tvUserName, tvUserRole;
    private LoginViewModel loginViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        
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
        setupUserInfo();
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }
    }
    
    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        toolbar = findViewById(R.id.toolbar);
        
        // Get header views
        View headerView = navigationView.getHeaderView(0);
        tvUserName = headerView.findViewById(R.id.tvUserName);
        tvUserRole = headerView.findViewById(R.id.tvUserRole);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dashboard");
        
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }
    
    private void setupNavigation() {
        navigationView.setNavigationItemSelectedListener(this);
        
        // Setup bottom navigation for tablet
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                loadFragment(new DashboardFragment());
                getSupportActionBar().setTitle("Dashboard");
                return true;
            } else if (itemId == R.id.nav_products) {
                loadFragment(new ProdukFragment());
                getSupportActionBar().setTitle("Produk");
                return true;
            } else if (itemId == R.id.nav_transaction) {
                loadFragment(new TransaksiFragment());
                getSupportActionBar().setTitle("Transaksi");
                return true;
            } else if (itemId == R.id.nav_history) {
                loadFragment(new HistoryFragment());
                getSupportActionBar().setTitle("Riwayat");
                return true;
            } else if (itemId == R.id.nav_report) {
                loadFragment(new ReportFragment());
                getSupportActionBar().setTitle("Laporan");
                return true;
            }
            return false;
        });
    }
    
    private void setupUserInfo() {
        String userName = loginViewModel.getCurrentUserName();
        String userRole = loginViewModel.isAdmin() ? "Administrator" : "Cashier";
        
        tvUserName.setText(userName);
        tvUserRole.setText(userRole);
        
        // Hide admin-only menu items for regular users
        if (!loginViewModel.isAdmin()) {
            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.nav_users).setVisible(false);
            menu.findItem(R.id.nav_settings).setVisible(false);
        }
    }
    
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        
        // Close drawer if open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, id.tugas.pos.ui.login.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_dashboard) {
            loadFragment(new DashboardFragment());
            getSupportActionBar().setTitle("Dashboard");
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
        } else if (itemId == R.id.nav_report) {
            loadFragment(new ReportFragment());
            getSupportActionBar().setTitle("Laporan");
        } else if (itemId == R.id.nav_settings) {
            loadFragment(new SettingsFragment());
            getSupportActionBar().setTitle("Pengaturan");
        } else if (itemId == R.id.nav_logout) {
            logout();
        }
        
        return true;
    }
    
    private void logout() {
        loginViewModel.logout();
        navigateToLogin();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 