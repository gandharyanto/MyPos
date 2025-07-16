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
} 