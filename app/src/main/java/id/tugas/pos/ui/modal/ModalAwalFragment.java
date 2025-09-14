package id.tugas.pos.ui.modal;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import id.tugas.pos.R;
import id.tugas.pos.data.model.ModalAwal;
import id.tugas.pos.databinding.DialogTambahModalBinding;
import id.tugas.pos.databinding.FragmentModalAwalBinding;
import id.tugas.pos.utils.PreferenceHelper;
import id.tugas.pos.viewmodel.ModalAwalViewModel;

public class ModalAwalFragment extends Fragment {
    private FragmentModalAwalBinding binding;
    private ModalAwalViewModel viewModel;
    private ModalAwalAdapter adapter;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;
    private int currentStoreId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentModalAwalBinding.inflate(inflater, container, false);

        initializeComponents();
        setupRecyclerView();
        setupObservers();
        setupClickListeners();
        loadData();

        return binding.getRoot();
    }

    private void initializeComponents() {
        viewModel = new ViewModelProvider(this).get(ModalAwalViewModel.class);
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        currentStoreId = PreferenceHelper.getCurrentStoreId(getContext());
    }

    private void setupRecyclerView() {
        adapter = new ModalAwalAdapter();
        binding.recyclerViewModalAwal.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewModalAwal.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getCurrentModal(currentStoreId).observe(getViewLifecycleOwner(), currentModal -> {
            if (currentModal != null) {
                binding.tvCurrentModal.setText(currencyFormat.format(currentModal));
                binding.tvLastUpdate.setText("Terakhir diperbarui: " + dateFormat.format(new Date()));
            } else {
                binding.tvCurrentModal.setText("Rp 0");
                binding.tvLastUpdate.setText("Terakhir diperbarui: -");
            }
        });

        viewModel.getAllModalAwal(currentStoreId).observe(getViewLifecycleOwner(), modalAwalList -> {
            updateUI(modalAwalList);
        });
    }

    private void setupClickListeners() {
        binding.btnTambahModal.setOnClickListener(v -> showTambahModalDialog());
        binding.btnSetModalAwal.setOnClickListener(v -> showSetModalAwalDialog());
    }

    private void loadData() {
        viewModel.loadModalAwal(currentStoreId);
    }

    private void updateUI(List<ModalAwal> modalAwalList) {
        if (modalAwalList != null && !modalAwalList.isEmpty()) {
            binding.recyclerViewModalAwal.setVisibility(View.VISIBLE);
            binding.tvEmptyState.setVisibility(View.GONE);
            adapter.setModalAwalList(modalAwalList);
        } else {
            binding.recyclerViewModalAwal.setVisibility(View.GONE);
            binding.tvEmptyState.setVisibility(View.VISIBLE);
        }
    }

    private void showTambahModalDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_tambah_modal, null);
        DialogTambahModalBinding dialogBinding = DialogTambahModalBinding.bind(dialogView);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Set current modal
        viewModel.getCurrentModal(currentStoreId).observe(getViewLifecycleOwner(), currentModal -> {
            double modal = currentModal != null ? currentModal : 0.0;
            dialogBinding.tvCurrentModalDialog.setText(currencyFormat.format(modal));
        });

        // Setup text watcher for real-time calculation
        dialogBinding.etNominal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePrediction(dialogBinding, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnSave.setOnClickListener(v -> {
            String nominalStr = dialogBinding.etNominal.getText().toString().trim();
            String keterangan = dialogBinding.etKeterangan.getText().toString().trim();

            if (nominalStr.isEmpty()) {
                Toast.makeText(getContext(), "Nominal tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double nominal = Double.parseDouble(nominalStr);
                if (nominal <= 0) {
                    Toast.makeText(getContext(), "Nominal harus lebih dari 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveModalAwal(nominal, "ADD_CAPITAL", keterangan);
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Format nominal tidak valid", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showSetModalAwalDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_tambah_modal, null);
        DialogTambahModalBinding dialogBinding = DialogTambahModalBinding.bind(dialogView);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Change title and button text for initial modal
        // You might want to create a separate layout for this, but reusing for simplicity

        dialogBinding.tvCurrentModalDialog.setText("Rp 0");
        dialogBinding.btnSave.setText("Set Modal Awal");

        dialogBinding.etNominal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String nominalStr = s.toString().trim();
                if (!nominalStr.isEmpty()) {
                    try {
                        double nominal = Double.parseDouble(nominalStr);
                        dialogBinding.layoutPrediction.setVisibility(View.VISIBLE);
                        dialogBinding.tvPredictedModal.setText(currencyFormat.format(nominal));
                    } catch (NumberFormatException e) {
                        dialogBinding.layoutPrediction.setVisibility(View.GONE);
                    }
                } else {
                    dialogBinding.layoutPrediction.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnSave.setOnClickListener(v -> {
            String nominalStr = dialogBinding.etNominal.getText().toString().trim();
            String keterangan = dialogBinding.etKeterangan.getText().toString().trim();

            if (nominalStr.isEmpty()) {
                Toast.makeText(getContext(), "Nominal tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double nominal = Double.parseDouble(nominalStr);
                if (nominal <= 0) {
                    Toast.makeText(getContext(), "Nominal harus lebih dari 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveModalAwal(nominal, "INITIAL", keterangan);
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Format nominal tidak valid", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void updatePrediction(DialogTambahModalBinding dialogBinding, String nominalStr) {
        if (!nominalStr.isEmpty()) {
            try {
                double nominal = Double.parseDouble(nominalStr);
                viewModel.getCurrentModal(currentStoreId).observe(getViewLifecycleOwner(), currentModal -> {
                    double modal = currentModal != null ? currentModal : 0.0;
                    double predicted = modal + nominal;
                    dialogBinding.layoutPrediction.setVisibility(View.VISIBLE);
                    dialogBinding.tvPredictedModal.setText(currencyFormat.format(predicted));
                });
            } catch (NumberFormatException e) {
                dialogBinding.layoutPrediction.setVisibility(View.GONE);
            }
        } else {
            dialogBinding.layoutPrediction.setVisibility(View.GONE);
        }
    }

    private void saveModalAwal(double nominal, String tipe, String keterangan) {
        long tanggal = System.currentTimeMillis();
        ModalAwal modalAwal = new ModalAwal(tanggal, currentStoreId, nominal, tipe, keterangan);

        viewModel.insertModalAwal(modalAwal);
        Toast.makeText(getContext(), "Modal berhasil disimpan", Toast.LENGTH_SHORT).show();
        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
