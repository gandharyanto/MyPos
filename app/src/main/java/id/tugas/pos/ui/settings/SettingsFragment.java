package id.tugas.pos.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;
import android.text.TextUtils;
import com.google.android.material.button.MaterialButton;
import androidx.lifecycle.ViewModelProvider;
import id.tugas.pos.viewmodel.LoginViewModel;
import id.tugas.pos.viewmodel.StoreViewModel;
import id.tugas.pos.data.model.Store;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import id.tugas.pos.R;

public class SettingsFragment extends Fragment {
    
    private LoginViewModel loginViewModel;
    private StoreViewModel storeViewModel;
    private AlertDialog currentDialog; // Tambahkan field untuk menyimpan dialog

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText("Pengaturan Aplikasi");

        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        storeViewModel = new ViewModelProvider(requireActivity()).get(StoreViewModel.class);
        
        MaterialButton btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        
        // Tambah tombol manajemen toko (hanya untuk admin)
        MaterialButton btnManageStore = view.findViewById(R.id.btnManageStore);
        if (btnManageStore != null) {
            loginViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
                if (user != null && user.isAdmin()) {
                    btnManageStore.setVisibility(View.VISIBLE);
                    btnManageStore.setOnClickListener(v -> showAddStoreDialog());
                } else {
                    btnManageStore.setVisibility(View.GONE);
                }
            });
        }
        
        return view;
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_ubah_password, null, false);
        EditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        
        AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
            .setTitle("Ubah Password")
            .setView(dialogView)
            .setPositiveButton("Simpan", (dialogInterface, which) -> {
                String oldPass = etOldPassword.getText().toString().trim();
                String newPass = etNewPassword.getText().toString().trim();
                String confirmPass = etConfirmPassword.getText().toString().trim();
                if (TextUtils.isEmpty(oldPass) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirmPass)) {
                    Toast.makeText(getContext(), "Semua field wajib diisi", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!newPass.equals(confirmPass)) {
                    Toast.makeText(getContext(), "Password baru tidak sama", Toast.LENGTH_SHORT).show();
                    return;
                }
                loginViewModel.changePassword(oldPass, newPass, result -> {
                    if (result) {
                        Toast.makeText(getContext(), "Password berhasil diubah", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Password lama salah", Toast.LENGTH_SHORT).show();
                    }
                });
                currentDialog = null;
            })
            .setNegativeButton("Batal", (dialogInterface, which) -> {
                currentDialog = null;
            })
            .create();
        
        // Simpan dialog untuk cleanup
        currentDialog = dialog;
        dialog.show();
    }

    private void showAddStoreDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_tambah_toko, null, false);
        EditText etStoreName = dialogView.findViewById(R.id.etStoreName);
        EditText etStoreAddress = dialogView.findViewById(R.id.etStoreAddress);
        EditText etStorePhone = dialogView.findViewById(R.id.etStorePhone);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);

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
            String storeName = etStoreName.getText().toString().trim();
            String storeAddress = etStoreAddress.getText().toString().trim();
            String storePhone = etStorePhone.getText().toString().trim();
            
            // Validasi
            if (TextUtils.isEmpty(storeName)) {
                etStoreName.setError("Nama toko wajib diisi");
                return;
            }
            if (TextUtils.isEmpty(storeAddress)) {
                etStoreAddress.setError("Alamat toko wajib diisi");
                return;
            }
            
            // Simpan toko baru
            Store newStore = new Store(storeName, storeAddress, storePhone);
            storeViewModel.addStore(newStore);
            Toast.makeText(getContext(), "Toko berhasil ditambahkan", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            currentDialog = null;
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