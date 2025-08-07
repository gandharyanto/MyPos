package id.tugas.pos.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import id.tugas.pos.R;
import id.tugas.pos.ui.MainActivity;
import id.tugas.pos.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    
    private LoginViewModel loginViewModel;
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private View progressBar, errorMessage;
    private boolean loginNavigated = false;
    
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
        
        setContentView(R.layout.activity_login);
        
        // Initialize ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        
        // Initialize views
        initViews();
        setupListeners();
        observeViewModel();
        
        // Create default users if needed
        createDefaultUsers();
    }
    
    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        errorMessage = findViewById(R.id.tvErrorMessage);
    }
    
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        
        // Handle Enter key press
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            performLogin();
            return true;
        });
    }
    
    private void observeViewModel() {
        // Observe loading state
        loginViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnLogin.setEnabled(!isLoading);
            etUsername.setEnabled(!isLoading);
            etPassword.setEnabled(!isLoading);
        });
        
        // Observe error messages
        loginViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                errorMessage.setVisibility(View.VISIBLE);
                ((android.widget.TextView) errorMessage).setText(error);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            } else {
                errorMessage.setVisibility(View.GONE);
            }
        });
        
        loginViewModel.getCurrentUser().observe(this, user -> {
            Log.d("LoginActivity", "CurrentUser: " + user);
            if (user != null && !loginNavigated) {
                loginNavigated = true;
                navigateToMainActivity();
            }
        });
    }
    
    private void performLogin() {
        String identifier = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (identifier.isEmpty()) {
            etUsername.setError("Username/Email tidak boleh kosong");
            return;
        }
        
        if (password.isEmpty()) {
            etPassword.setError("Password tidak boleh kosong");
            return;
        }
        
        // Login dengan email atau username (tidak perlu membedakan admin/user)
        loginViewModel.login(identifier, password);
    }
    
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void createDefaultUsers() {
        // Initialize database dengan user default hanya jika database kosong
        loginViewModel.initializeDatabaseIfNeeded();
    }
    
    @Override
    public void onBackPressed() {
        // Prevent going back from login screen
        // You might want to show a confirmation dialog here
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        super.onBackPressed();
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
} 