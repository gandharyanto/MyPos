package id.tugas.pos.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import id.tugas.pos.data.model.User;
import id.tugas.pos.data.repository.UserRepository;

public class LoginViewModel extends AndroidViewModel {
    
    private UserRepository userRepository;
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    
    private static final String PREFS_NAME = "session";
    private static final String KEY_USER_ID = "userId";
    private Context context;
    
    public LoginViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
        this.context = application.getApplicationContext();
        // Load session if exists
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID, -1);
        if (userId != -1) {
            userRepository.getUserById(userId).observeForever(user -> {
                if (user != null) {
                    currentUser.setValue(user);
                }
            });
        }
    }
    
    public void login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            errorMessage.setValue("Username tidak boleh kosong");
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            errorMessage.setValue("Password tidak boleh kosong");
            return;
        }
        
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        // In a real app, you would hash the password before comparing
        LiveData<User> userLiveData = userRepository.login(username.trim(), password);
        userLiveData.observeForever(user -> {
            if (user != null) {
                currentUser.setValue(user);
                // Simpan session ke SharedPreferences
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().putInt(KEY_USER_ID, user.getId()).apply();
                // Update last login time
                userRepository.updateLastLogin(user.getId(), System.currentTimeMillis());
                isLoading.setValue(false);
            } else {
                errorMessage.setValue("Username atau password salah");
                isLoading.setValue(false);
            }
            userLiveData.removeObserver(user1 -> {});
        });
    }
    
    public void logout() {
        currentUser.setValue(null);
        // Hapus session dari SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_USER_ID).apply();
    }
    
    public void createDefaultAdmin() {
        // Admin utama dengan email aidilfitriyoka2812@gmail.com
        User admin = new User("admin", "aidilfitriyoka2812@gmail.com", "admin123", "Administrator", "ADMIN");
        userRepository.insert(admin);
        
        // User biasa untuk testing
        User user = new User("user", "user123", "Cashier", "USER");
        userRepository.insert(user);
    }

    public void createAdminWithStore(String email, String password, String fullName, int storeId) {
        // Validasi email admin
        if (!email.equals("aidilfitriyoka2812@gmail.com")) {
            errorMessage.setValue("Admin hanya bisa didaftarkan melalui email: aidilfitriyoka2812@gmail.com");
            return;
        }
        
        User admin = new User("admin_" + storeId, email, password, fullName, "ADMIN");
        admin.setStoreId(storeId); // Set storeId untuk admin
        userRepository.insert(admin);
    }

    public void loginWithEmail(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Email tidak boleh kosong");
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            errorMessage.setValue("Password tidak boleh kosong");
            return;
        }
        
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        // Login menggunakan email untuk admin
        LiveData<User> userLiveData = userRepository.loginByEmail(email.trim(), password);
        userLiveData.observeForever(user -> {
            if (user != null) {
                currentUser.setValue(user);
                // Simpan session ke SharedPreferences
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().putInt(KEY_USER_ID, user.getId()).apply();
                // Update last login time
                userRepository.updateLastLogin(user.getId(), System.currentTimeMillis());
                isLoading.setValue(false);
            } else {
                errorMessage.setValue("Email atau password salah");
                isLoading.setValue(false);
            }
            userLiveData.removeObserver(user1 -> {});
        });
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser.getValue() != null;
    }
    
    public boolean isAdmin() {
        User user = currentUser.getValue();
        return user != null && user.isAdmin();
    }
    
    public boolean isUser() {
        User user = currentUser.getValue();
        return user != null && user.isUser();
    }
    
    public String getCurrentUserName() {
        User user = currentUser.getValue();
        return user != null ? user.getFullName() : "";
    }
    
    public int getCurrentUserId() {
        User user = currentUser.getValue();
        return user != null ? user.getId() : -1;
    }

    public void changePassword(String oldPassword, String newPassword, ChangePasswordCallback callback) {
        User user = currentUser.getValue();
        if (user == null) {
            callback.onResult(false);
            return;
        }
        // Cek password lama
        if (!user.getPassword().equals(oldPassword)) {
            callback.onResult(false);
            return;
        }
        user.setPassword(newPassword);
        userRepository.update(user);
        callback.onResult(true);
    }

    public interface ChangePasswordCallback {
        void onResult(boolean success);
    }
} 