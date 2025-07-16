package id.tugas.pos.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import id.tugas.pos.data.model.User;
import id.tugas.pos.data.repository.UserRepository;

public class UserManagementViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    public UserManagementViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }
    public LiveData<List<User>> getAllUsers() {
        return userRepository.getAllActiveUsers();
    }

    public void addUser(String fullName, String email, String username, String password, String role, int storeId) {
        User user;
        if ("ADMIN".equals(role)) {
            user = new User(username != null && !username.isEmpty() ? username : email, email, password, fullName, role);
        } else {
            user = new User(username, password, fullName, role);
            user.setEmail(email);
        }
        user.setStoreId(storeId);
        userRepository.insert(user);
    }
} 