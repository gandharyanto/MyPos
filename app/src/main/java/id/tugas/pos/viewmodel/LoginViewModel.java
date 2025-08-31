package id.tugas.pos.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

import id.tugas.pos.data.model.User;
import id.tugas.pos.data.repository.UserRepository;
import id.tugas.pos.data.repository.StoreRepository;
import id.tugas.pos.data.repository.ProductRepository;
import id.tugas.pos.data.model.Store;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginViewModel extends AndroidViewModel {
    
    private UserRepository userRepository;
    private StoreRepository storeRepository;
    private ProductRepository productRepository;
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private ExecutorService executorService;
    
    private static final String PREFS_NAME = "session";
    private static final String KEY_USER_ID = "userId";
    private Context context;
    private static final String TAG = "LoginViewModel";
    
    // Keep reference to session observer
    private androidx.lifecycle.Observer<User> sessionObserver;

    public LoginViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
        storeRepository = new StoreRepository(application);
        productRepository = new ProductRepository(application);
        this.context = application.getApplicationContext();
        this.executorService = Executors.newSingleThreadExecutor();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID, -1);
        Log.d(TAG, "Constructor: Loaded userId from prefs: " + userId);
        if (userId != -1) {
            Log.d(TAG, "Constructor: Attempting to restore session for userId: " + userId);
            sessionObserver = user -> {
                Log.d(TAG, "Session restore: user loaded from DB: " + new Gson().toJson(user));
                if (user != null) {
                    currentUser.setValue(user);
                    Log.d(TAG, "Session restore: currentUser set to: " + user);
                } else {
                    Log.d(TAG, "Session restore: user is null, clearing session");
                    currentUser.setValue(null);
                    // Clear invalid session
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove(KEY_USER_ID);
                    editor.apply();
                    Log.d(TAG, "Session restore: Invalid session cleared");
                }
            };
            userRepository.getUserById(userId).observeForever(sessionObserver);
        } else {
            Log.d(TAG, "Constructor: No session found in prefs");
        }
    }
    
    public void clearSessionObserver() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID, -1);
        if (userId != -1 && sessionObserver != null) {
            userRepository.getUserById(userId).removeObserver(sessionObserver);
            sessionObserver = null;
        }
    }

    public void login(String identifier, String password) {
        Log.d(TAG, "login called with identifier: " + identifier);
        if (identifier == null || identifier.trim().isEmpty()) {
            errorMessage.setValue("Username/Email tidak boleh kosong");
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            errorMessage.setValue("Password tidak boleh kosong");
            return;
        }
        
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        // Login dengan email atau username
        LiveData<User> userLiveData = userRepository.loginWithEmailOrUsername(identifier.trim(), password);
        androidx.lifecycle.Observer<User> loginObserver = new androidx.lifecycle.Observer<User>() {
            @Override
            public void onChanged(User user) {
                Log.d(TAG, "login: user loaded: " + user);
                if (user != null) {
                    currentUser.setValue(user);
                    Log.d(TAG, "login: currentUser set to: " + user);
                    // Refresh produk setelah login sukses
                    if (productRepository != null) {
                        productRepository.refreshAllProducts();
                    }
                    // Simpan session ke SharedPreferences
                    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    prefs.edit().putInt(KEY_USER_ID, user.getId()).apply();
                    // Update last login time
                    userRepository.updateLastLogin(user.getId(), System.currentTimeMillis());
                    loginResult.setValue(new LoginResult(true));
                    isLoading.setValue(false);
                } else {
                    Log.d(TAG, "login: user is null");
                    errorMessage.setValue("Username/Email atau password salah");
                    loginResult.setValue(new LoginResult(false));
                    isLoading.setValue(false);
                }
                userLiveData.removeObserver(this);
            }
        };
        userLiveData.observeForever(loginObserver);
    }
    
    public void logout() {
        Log.d(TAG, "logout called, clearing currentUser");
        currentUser.setValue(null);
        // Hapus session dari SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_USER_ID).apply();
        clearSessionObserver();
    }
    
    public void createDefaultAdmin() {
        // Jalankan operasi database di background thread
        executorService.execute(() -> {
            // Cek apakah admin sudah ada
            if (!userRepository.isEmailExists("aidilfitriyoka2812@gmail.com")) {
                // Admin utama dengan email aidilfitriyoka2812@gmail.com dan username admin
                User admin = new User("admin", "aidilfitriyoka2812@gmail.com", "admin123", "Administrator", "ADMIN");
                userRepository.insert(admin);
            }
        });
    }

    public void initializeDatabaseIfNeeded() {
        // Jalankan operasi database di background thread
        executorService.execute(() -> {
            // Cek apakah database sudah diinisialisasi dengan mengecek jumlah user
            List<User> existingUsers = userRepository.getAllActiveUsersSync();
            if (existingUsers == null || existingUsers.isEmpty()) {
                // Database kosong, buat user default
                createDefaultAdmin();
            } else {
                // Cek dan bersihkan duplikat jika ada
                cleanupDuplicateUsers(existingUsers);
            }
        });
    }

    private void cleanupDuplicateUsers(List<User> users) {
        // Jalankan operasi database di background thread
        executorService.execute(() -> {
            // Cek duplikat berdasarkan email dan username
            for (int i = 0; i < users.size(); i++) {
                for (int j = i + 1; j < users.size(); j++) {
                    User user1 = users.get(i);
                    User user2 = users.get(j);
                    
                    // Hapus duplikat berdasarkan email
                    if (user1.getEmail() != null && user1.getEmail().equals(user2.getEmail())) {
                        // Hapus user yang lebih baru (ID lebih besar)
                        if (user1.getId() > user2.getId()) {
                            userRepository.delete(user1);
                        } else {
                            userRepository.delete(user2);
                        }
                    }
                    
                    // Hapus duplikat berdasarkan username
                    if (user1.getUsername() != null && user1.getUsername().equals(user2.getUsername())) {
                        // Hapus user yang lebih baru (ID lebih besar)
                        if (user1.getId() > user2.getId()) {
                            userRepository.delete(user1);
                        } else {
                            userRepository.delete(user2);
                        }
                    }
                }
            }
        });
    }

    public void createAdminWithStore(String email, String password, String fullName, int storeId) {
        // Validasi email admin
        if (!email.equals("aidilfitriyoka2812@gmail.com")) {
            errorMessage.setValue("Admin hanya bisa didaftarkan melalui email: aidilfitriyoka2812@gmail.com");
            return;
        }
        
        // Jalankan operasi database di background thread
        executorService.execute(() -> {
            // Cek apakah admin dengan email ini sudah ada
            if (userRepository.isEmailExists(email)) {
                errorMessage.postValue("Admin dengan email ini sudah ada");
                return;
            }
            
            User admin = new User("admin_" + storeId, email, password, fullName, "ADMIN");
            admin.setStoreId(storeId); // Set storeId untuk admin
            userRepository.insert(admin);
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
    
    public LiveData<LoginResult> getLoginResult() { return loginResult; }
    
    public LiveData<List<Store>> getStores() {
        MutableLiveData<List<Store>> liveData = new MutableLiveData<>();
        storeRepository.getAllStores().observeForever(stores -> {
            if (stores != null) {
                Store allStore = new Store();
                allStore.setId(-1);
                allStore.setName("Semua Toko");
                allStore.setAddress("");
                allStore.setPhone("");
                List<Store> newList = new java.util.ArrayList<>();
                newList.add(allStore);
                newList.addAll(stores);
                liveData.postValue(newList);
            } else {
                liveData.postValue(null);
            }
        });
        return liveData;
    }
    
    // Method untuk ProdukFragment - tidak ada opsi "Semua Toko"
    public LiveData<List<Store>> getStoresForProduct() {
        return storeRepository.getAllStores();
    }
    
    public LiveData<Store> getStoreById(int storeId) {
        return storeRepository.getStoreById(storeId);
    }
    
    public boolean isLoggedIn() {
        return currentUser.getValue() != null;
    }
    
    public boolean isAdmin() {
        User user = currentUser.getValue();
        Log.d(TAG, "isAdmin called, user: " + new Gson().toJson(user));
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
        
        // Jalankan operasi database di background thread
        executorService.execute(() -> {
            // Cek password lama
            if (!user.getPassword().equals(oldPassword)) {
                callback.onResult(false);
                return;
            }
            user.setPassword(newPassword);
            userRepository.update(user);
            callback.onResult(true);
        });
    }

    public interface ChangePasswordCallback {
        void onResult(boolean success);
    }
    
    public static class LoginResult {
        private boolean success;
        public LoginResult(boolean success) { this.success = success; }
        public boolean isSuccess() { return success; }
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
