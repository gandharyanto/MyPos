package id.tugas.pos.viewmodel;

import android.app.Application;

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
    
    public LoginViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
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
                // Update last login time
                userRepository.updateLastLogin(user.getId(), System.currentTimeMillis());
                isLoading.setValue(false);
            } else {
                errorMessage.setValue("Username atau password salah");
                isLoading.setValue(false);
            }
            // Remove observer to prevent memory leaks
            userLiveData.removeObserver(user1 -> {});
        });
    }
    
    public void logout() {
        currentUser.setValue(null);
    }
    
    public void createDefaultAdmin() {
        User admin = new User("admin", "admin123", "Administrator", "ADMIN");
        userRepository.insert(admin);
        
        User user = new User("user", "user123", "Cashier", "USER");
        userRepository.insert(user);
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
} 