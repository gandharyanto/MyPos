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
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.text.TextUtils;
import android.widget.Toast;
import android.text.TextWatcher;
import id.tugas.pos.viewmodel.StoreViewModel;
import id.tugas.pos.data.model.Store;
import android.os.Handler;
import android.os.Looper;

public class UserManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private FloatingActionButton fabAddUser;
    private UserManagementViewModel userManagementViewModel;
    private LoginViewModel loginViewModel;
    private StoreViewModel storeViewModel;
    private AlertDialog currentDialog; // Tambahkan field untuk menyimpan dialog
    private Handler mainHandler; // Handler untuk main thread

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
        mainHandler = new Handler(Looper.getMainLooper()); // Inisialisasi Handler untuk main thread
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
                // Bersihkan duplikat jika ada
                userManagementViewModel.cleanupDuplicateUsers();
                
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
        AutoCompleteTextView spinnerRole = dialogView.findViewById(R.id.spinnerRole);
        AutoCompleteTextView spinnerStore = dialogView.findViewById(R.id.spinnerStore);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Setup role spinner
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new String[]{"User"});
        spinnerRole.setAdapter(roleAdapter);
        spinnerRole.setText("User", false);

        // Setup store spinner
        storeViewModel.getAllStores().observe(getViewLifecycleOwner(), stores -> {
            ArrayAdapter<String> storeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line);
            for (Store store : stores) {
                storeAdapter.add(store.getName());
            }
            spinnerStore.setAdapter(storeAdapter);
        });

        // Validasi real-time untuk username
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String username = s.toString().trim();
                if (!TextUtils.isEmpty(username)) {
                    userManagementViewModel.isUsernameExists(username, exists -> {
                        // Gunakan Handler untuk memastikan operasi UI dilakukan di main thread
                        mainHandler.post(() -> {
                            if (getActivity() != null && !getActivity().isFinishing()) {
                                if (exists) {
                                    etUsername.setError("Username sudah digunakan");
                                } else {
                                    etUsername.setError(null);
                                }
                            }
                        });
                    });
                } else {
                    mainHandler.post(() -> {
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            etUsername.setError(null);
                        }
                    });
                }
            }
        });

        // Validasi real-time untuk email
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String email = s.toString().trim();
                if (!TextUtils.isEmpty(email)) {
                    userManagementViewModel.isEmailExists(email, exists -> {
                        // Gunakan Handler untuk memastikan operasi UI dilakukan di main thread
                        mainHandler.post(() -> {
                            if (getActivity() != null && !getActivity().isFinishing()) {
                                if (exists) {
                                    etEmail.setError("Email sudah digunakan");
                                } else {
                                    etEmail.setError(null);
                                }
                            }
                        });
                    });
                } else {
                    mainHandler.post(() -> {
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            etEmail.setError(null);
                        }
                    });
                }
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        
        // Simpan dialog untuk cleanup
        currentDialog = dialog;

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            currentDialog = null;
        });
        btnSave.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = "USER"; // Hanya bisa menambah user, bukan admin
            String selectedStoreName = spinnerStore.getText().toString().trim();

            // Validasi
            boolean hasError = false;
            if (TextUtils.isEmpty(fullName)) {
                etFullName.setError("Nama lengkap wajib diisi");
                hasError = true;
            }
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email wajib diisi");
                hasError = true;
            }
            if (TextUtils.isEmpty(username)) {
                etUsername.setError("Username wajib diisi");
                hasError = true;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password wajib diisi");
                hasError = true;
            }
            if (TextUtils.isEmpty(selectedStoreName)) {
                Toast.makeText(getContext(), "Pilih toko", Toast.LENGTH_SHORT).show();
                hasError = true;
            }
            
            if (hasError) {
                return;
            }
            
            // Validasi email unik
            userManagementViewModel.isEmailExists(email, emailExists -> {
                if (emailExists) {
                    mainHandler.post(() -> {
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            etEmail.setError("Email sudah digunakan");
                        }
                    });
                    return;
                }
                
                // Validasi username unik
                userManagementViewModel.isUsernameExists(username, usernameExists -> {
                    if (usernameExists) {
                        mainHandler.post(() -> {
                            if (getActivity() != null && !getActivity().isFinishing()) {
                                etUsername.setError("Username sudah digunakan");
                            }
                        });
                        return;
                    }
                    
                    // Cari store berdasarkan nama
                    final Store[] selectedStore = {null};
                    if (storeViewModel.getAllStores().getValue() != null) {
                        for (Store store : storeViewModel.getAllStores().getValue()) {
                            if (store.getName().equals(selectedStoreName)) {
                                selectedStore[0] = store;
                                break;
                            }
                        }
                    }
                    
                    if (selectedStore[0] == null) {
                        mainHandler.post(() -> {
                            if (getActivity() != null && !getActivity().isFinishing()) {
                                Toast.makeText(getContext(), "Toko tidak ditemukan", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                    
                    // Simpan user baru
                    userManagementViewModel.addUser(fullName, email, username, password, role, selectedStore[0].getId());
                    mainHandler.post(() -> {
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            Toast.makeText(getContext(), "User berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            currentDialog = null;
                        }
                    });
                });
            });
        });
        dialog.show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Dismiss dialog jika masih terbuka untuk mencegah window leak
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Dismiss dialog jika masih terbuka untuk mencegah window leak
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }
    }
} 