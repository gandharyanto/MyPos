package id.tugas.pos.ui.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import id.tugas.pos.R;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.widget.TextView;
import id.tugas.pos.utils.ExcelExporter;

public class ReportPengeluaranFragment extends Fragment {
    private LaporanPengeluaranAdapter adapter;
    private ReportPengeluaranViewModel viewModel;
    private long startDate = 0;
    private long endDate = 0;
    private TextView tvHeader;
    private TextView tvTanggalDipilih;
    private Integer selectedStoreId = null;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report_pengeluaran, container, false);
        Button btnExportPdf = new Button(requireContext());
        btnExportPdf.setText("Export PDF");
        Button btnExportExcel = new Button(requireContext());
        btnExportExcel.setText("Export Excel");
        LinearLayout layoutContainer = view.findViewById(R.id.layoutContainer);
        layoutContainer.addView(btnExportPdf, 0);
        layoutContainer.addView(btnExportExcel, 1);
        btnExportPdf.setOnClickListener(v -> exportPdf());
        btnExportExcel.setOnClickListener(v -> exportExcel());

        RecyclerView rv = view.findViewById(R.id.rvLaporanPengeluaran);
        adapter = new LaporanPengeluaranAdapter();
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())).get(ReportPengeluaranViewModel.class);
        viewModel.getLaporanPengeluaran().observe(getViewLifecycleOwner(), adapter::setData);

        tvHeader = view.findViewById(R.id.tvHeader);
        tvTanggalDipilih = view.findViewById(R.id.tvTanggalDipilih);
        Button btnFilterTanggal = view.findViewById(R.id.btnFilterTanggal);
        btnFilterTanggal.setOnClickListener(v -> showDateRangePicker());

        // Setup store spinner for admin
        id.tugas.pos.ui.MainActivity mainActivity = (id.tugas.pos.ui.MainActivity) requireActivity();
        id.tugas.pos.viewmodel.LoginViewModel loginViewModel = new ViewModelProvider(requireActivity()).get(id.tugas.pos.viewmodel.LoginViewModel.class);
        id.tugas.pos.viewmodel.StoreViewModel storeViewModel = new ViewModelProvider(requireActivity()).get(id.tugas.pos.viewmodel.StoreViewModel.class);
        if (loginViewModel.isAdmin()) {
            mainActivity.spinnerStore.setVisibility(View.VISIBLE);
            mainActivity.labelStore.setVisibility(View.VISIBLE);
            storeViewModel.getSelectedStoreId().observe(getViewLifecycleOwner(), storeId -> {
                selectedStoreId = (storeId != null && storeId > 0) ? storeId : null;
                loadLaporan();
            });
        } else {
            loginViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
                selectedStoreId = (user != null && user.getStoreId() != null) ? user.getStoreId() : null;
                loadLaporan();
            });
        }
        // Load data awal (semua tanggal)
        startDate = 0;
        endDate = System.currentTimeMillis();
        loadLaporan();
        return view;
    }
    private void loadLaporan() {
        viewModel.setStoreId(selectedStoreId);
        viewModel.setDateRange(startDate, endDate);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String tgl = (startDate == 0 ? "Semua Tanggal" : sdf.format(new Date(startDate)) + " - " + sdf.format(new Date(endDate)));
        tvHeader.setText("Laporan Pengeluaran\n" + tgl);
        tvTanggalDipilih.setText(tgl);
    }
    private void showDateRangePicker() {
        MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Pilih Rentang Tanggal");
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            startDate = selection.first;
            endDate = selection.second;
            loadLaporan();
        });
        picker.show(getParentFragmentManager(), "date_range_picker");
    }
    private void exportPdf() {
        Toast.makeText(requireContext(), "Export PDF belum diimplementasikan", Toast.LENGTH_SHORT).show();
    }
    private void exportExcel() {
        List<LaporanPengeluaranItem> data = adapter.getData();
        if (data == null || data.isEmpty()) {
            Toast.makeText(requireContext(), "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        ExcelExporter.exportExpenseReport(requireContext(), data);
    }
}
