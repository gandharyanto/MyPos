package id.tugas.pos.ui.login;

import android.content.Intent;
import android.os.Bundle;
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        
        // Observe current user
        loginViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                // Login successful, navigate to main activity
                navigateToMainActivity();
            }
        });
    }
    
    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (username.isEmpty()) {
            etUsername.setError("Username/Email tidak boleh kosong");
            return;
        }
        
        if (password.isEmpty()) {
            etPassword.setError("Password tidak boleh kosong");
            return;
        }
        
        // Cek apakah input adalah email (untuk admin)
        if (isValidEmail(username)) {
            // Login dengan email untuk admin
            loginViewModel.loginWithEmail(username, password);
        } else {
            // Login dengan username untuk user biasa
            loginViewModel.login(username, password);
        }
    }
    
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void createDefaultUsers() {
        // This will create default admin and user accounts
        // In a real app, you might want to check if users already exist
        loginViewModel.createDefaultAdmin();
    }
    
    @Override
    public void onBackPressed() {
        // Prevent going back from login screen
        // You might want to show a confirmation dialog here
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }
} 