package id.tugas.pos.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import id.tugas.pos.R;
import id.tugas.pos.data.model.User;
import id.tugas.pos.viewmodel.LoginViewModel;
import id.tugas.pos.viewmodel.UserManagementViewModel;

public class UserManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private FloatingActionButton fabAddUser;
    private UserManagementViewModel userManagementViewModel;
    private LoginViewModel loginViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_management, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        fabAddUser = view.findViewById(R.id.fabAddUser);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter();
        recyclerView.setAdapter(userAdapter);
        userManagementViewModel = new ViewModelProvider(this).get(UserManagementViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        setupObservers();
        fabAddUser.setOnClickListener(v -> showAddUserDialog());
        return view;
    }

    private void setupObservers() {
        // Hanya admin yang bisa akses
        loginViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null || !user.isAdmin()) {
                // Tutup fragment jika bukan admin
                requireActivity().onBackPressed();
            } else {
                userManagementViewModel.getAllUsers().observe(getViewLifecycleOwner(), users -> {
                    userAdapter.setUsers(users);
                });
            }
        });
    }

    private void showAddUserDialog() {
        // TODO: Implementasi dialog tambah user
    }
} 