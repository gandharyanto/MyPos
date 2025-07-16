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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import id.tugas.pos.R;

public class SettingsFragment extends Fragment {
    
    private LoginViewModel loginViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        TextView tvTitle = view.findViewById(R.id.tvTitle);

        tvTitle.setText("Pengaturan Aplikasi");

        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        MaterialButton btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        
        return view;
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_ubah_password, null, false);
        EditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        new AlertDialog.Builder(getContext())
            .setTitle("Ubah Password")
            .setView(dialogView)
            .setPositiveButton("Simpan", (dialog, which) -> {
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
            })
            .setNegativeButton("Batal", null)
            .show();
    }
} 