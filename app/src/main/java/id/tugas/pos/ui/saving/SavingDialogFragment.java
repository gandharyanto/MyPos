package id.tugas.pos.ui.saving;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import id.tugas.pos.R;
import id.tugas.pos.data.model.Saving;
import id.tugas.pos.data.model.ModalAwal;
import id.tugas.pos.data.repository.ModalAwalRepository;
import id.tugas.pos.viewmodel.SavingViewModel;
import id.tugas.pos.viewmodel.DashboardViewModel;
import id.tugas.pos.viewmodel.LoginViewModel;
import java.util.Calendar;

public class SavingDialogFragment extends DialogFragment {
    private EditText etNominal, etKeterangan;
    private Button btnSimpan;
    private Spinner spinnerTipePengeluaran;
    private SavingViewModel savingViewModel;
    private DashboardViewModel dashboardViewModel;
    private LoginViewModel loginViewModel;
    private ModalAwalRepository modalAwalRepository;

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_saving, null, false);
        etNominal = view.findViewById(R.id.etNominal);
        etKeterangan = view.findViewById(R.id.etKeterangan);
        btnSimpan = view.findViewById(R.id.btnSimpan);
        spinnerTipePengeluaran = view.findViewById(R.id.spinnerTipePengeluaran);
        // Setup spinner tipe pengeluaran
        String[] tipeArray = {"Operasional", "Gaji", "Listrik", "Lainnya", "Saving"};
        ArrayAdapter<String> tipeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tipeArray);
        tipeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipePengeluaran.setAdapter(tipeAdapter);
        savingViewModel = new ViewModelProvider(requireActivity()).get(SavingViewModel.class);
        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        modalAwalRepository = new ModalAwalRepository(requireActivity().getApplication());
        btnSimpan.setOnClickListener(v -> {
            int nominal = 0;
            try { nominal = Integer.parseInt(etNominal.getText().toString()); } catch (Exception ignored) {}
            if (nominal <= 0) {
                Toast.makeText(getContext(), "Nominal harus > 0", Toast.LENGTH_SHORT).show();
                return;
            }
            dashboardViewModel.getTodaySales().observe(this, todaySales -> {
                double today = todaySales != null ? todaySales : 0.0;
                if (nominal > today) {
                    Toast.makeText(getContext(), "Nominal pengeluaran tidak boleh lebih dari pendapatan hari ini!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String ket = etKeterangan.getText().toString();
                String tipe = spinnerTipePengeluaran.getSelectedItem().toString();
                Saving saving = new Saving();
                saving.setAmount(nominal);
                saving.setDescription(ket + " [" + tipe + "]");
                saving.setSavingDate(System.currentTimeMillis());
                savingViewModel.insert(saving, () -> {
                    // Logic modal awal jika tipe Saving
                    if ("Saving".equalsIgnoreCase(tipe)) {
                        if (nominal < today) {
                            Calendar cal = Calendar.getInstance();
                            cal.add(Calendar.DATE, 1);
                            long besok = cal.get(Calendar.YEAR) * 10000 + (cal.get(Calendar.MONTH)+1) * 100 + cal.get(Calendar.DAY_OF_MONTH);
                            int storeId = 0;
                            if (loginViewModel.getCurrentUser().getValue() != null && loginViewModel.getCurrentUser().getValue().getStoreId() != null) {
                                storeId = loginViewModel.getCurrentUser().getValue().getStoreId();
                            }
                            ModalAwal modalAwal = new ModalAwal();
                            modalAwal.tanggal = besok;
                            modalAwal.storeId = storeId;
                            modalAwal.nominal = today - nominal;
                            modalAwalRepository.insert(modalAwal);
                        }
                    }
                    Toast.makeText(getContext(), "Pengeluaran berhasil disimpan", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            });
        });
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);
        dialog.setTitle("Pengeluaran/Tabung Kasir");
        return dialog;
    }
} 