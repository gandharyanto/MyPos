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
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.text.TextUtils;
import android.widget.Toast;
import id.tugas.pos.viewmodel.StoreViewModel;
import id.tugas.pos.data.model.Store;

public class UserManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private FloatingActionButton fabAddUser;
    private UserManagementViewModel userManagementViewModel;
    private LoginViewModel loginViewModel;
    private StoreViewModel storeViewModel;

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
        storeViewModel = new ViewModelProvider(requireActivity()).get(StoreViewModel.class);
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
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_tambah_user, null, false);
        EditText etFullName = dialogView.findViewById(R.id.etFullName);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        EditText etUsername = dialogView.findViewById(R.id.etUsername);
        EditText etPassword = dialogView.findViewById(R.id.etPassword);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerRole);
        Spinner spinnerStore = dialogView.findViewById(R.id.spinnerStore);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Setup role spinner
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new String[]{"Admin", "User"});
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        // Setup store spinner
        storeViewModel.getAllStores().observe(getViewLifecycleOwner(), stores -> {
            ArrayAdapter<String> storeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item);
            storeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            for (Store store : stores) {
                storeAdapter.add(store.getName());
            }
            spinnerStore.setAdapter(storeAdapter);
        });

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = spinnerRole.getSelectedItem().toString().equals("Admin") ? "ADMIN" : "USER";
            int storePosition = spinnerStore.getSelectedItemPosition();
            Store selectedStore = null;
            if (storeViewModel.getAllStores().getValue() != null && storePosition >= 0) {
                selectedStore = storeViewModel.getAllStores().getValue().get(storePosition);
            }
            // Validasi
            if (TextUtils.isEmpty(fullName)) {
                etFullName.setError("Nama lengkap wajib diisi");
                return;
            }
            if (role.equals("ADMIN")) {
                if (TextUtils.isEmpty(email)) {
                    etEmail.setError("Email wajib untuk admin");
                    return;
                }
            } else {
                if (TextUtils.isEmpty(username)) {
                    etUsername.setError("Username wajib untuk user");
                    return;
                }
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password wajib diisi");
                return;
            }
            if (selectedStore == null) {
                Toast.makeText(getContext(), "Pilih toko", Toast.LENGTH_SHORT).show();
                return;
            }
            // Simpan user baru
            userManagementViewModel.addUser(fullName, email, username, password, role, selectedStore.getId());
            dialog.dismiss();
        });
        dialog.show();
    }
} 