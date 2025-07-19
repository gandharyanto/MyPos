package id.tugas.pos.ui.saving;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import id.tugas.pos.R;
import id.tugas.pos.data.model.Saving;
import id.tugas.pos.viewmodel.SavingViewModel;

public class SavingDialogFragment extends DialogFragment {
    private EditText etNominal, etKeterangan;
    private Button btnSimpan;
    private SavingViewModel savingViewModel;

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_saving, null, false);
        etNominal = view.findViewById(R.id.etNominal);
        etKeterangan = view.findViewById(R.id.etKeterangan);
        btnSimpan = view.findViewById(R.id.btnSimpan);
        savingViewModel = new ViewModelProvider(requireActivity()).get(SavingViewModel.class);
        btnSimpan.setOnClickListener(v -> {
            int nominal = 0;
            try { nominal = Integer.parseInt(etNominal.getText().toString()); } catch (Exception ignored) {}
            if (nominal <= 0) {
                Toast.makeText(getContext(), "Nominal harus > 0", Toast.LENGTH_SHORT).show();
                return;
            }
            String ket = etKeterangan.getText().toString();
            Saving saving = new Saving();
            saving.setAmount(nominal);
            saving.setDescription(ket);
            saving.setSavingDate(System.currentTimeMillis());
            savingViewModel.insert(saving, () -> {
                Toast.makeText(getContext(), "Pengeluaran berhasil disimpan", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        });
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);
        dialog.setTitle("Pengeluaran/Tabung Kasir");
        return dialog;
    }
} 