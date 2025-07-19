package id.tugas.pos.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import id.tugas.pos.data.model.User;
import id.tugas.pos.data.repository.UserRepository;

public class UserManagementViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private ExecutorService executorService;
    
    public UserManagementViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        executorService = Executors.newSingleThreadExecutor();
    }
    
    public LiveData<List<User>> getAllUsers() {
        return userRepository.getAllActiveUsers();
    }

    public void addUser(String fullName, String email, String username, String password, String role, int storeId) {
        executorService.execute(() -> {
            User user = new User(username, email, password, fullName, role);
            user.setStoreId(storeId);
            userRepository.insert(user);
        });
    }
    
    public void isUsernameExists(String username, ValidationCallback callback) {
        executorService.execute(() -> {
            boolean exists = userRepository.isUsernameExists(username);
            callback.onResult(exists);
        });
    }
    
    public void isEmailExists(String email, ValidationCallback callback) {
        executorService.execute(() -> {
            boolean exists = userRepository.isEmailExists(email);
            callback.onResult(exists);
        });
    }
    
    public interface ValidationCallback {
        void onResult(boolean exists);
    }
    
    public void cleanupDuplicateUsers() {
        executorService.execute(() -> {
            // Bersihkan duplikat berdasarkan email
            List<User> adminUsers = userRepository.getUsersByEmail("aidilfitriyoka2812@gmail.com");
            if (adminUsers != null && adminUsers.size() > 1) {
                // Hapus semua kecuali yang pertama (ID terkecil)
                for (int i = 1; i < adminUsers.size(); i++) {
                    userRepository.delete(adminUsers.get(i));
                }
            }
            
            // Bersihkan duplikat berdasarkan username
            List<User> testUsers = userRepository.getUsersByUsername("user");
            if (testUsers != null && testUsers.size() > 1) {
                // Hapus semua kecuali yang pertama (ID terkecil)
                for (int i = 1; i < testUsers.size(); i++) {
                    userRepository.delete(testUsers.get(i));
                }
            }
        });
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
} 