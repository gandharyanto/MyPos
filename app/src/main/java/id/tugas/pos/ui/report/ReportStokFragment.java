package id.tugas.pos.ui.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.Locale;
import android.widget.TextView;
import id.tugas.pos.ui.stockin.StockInDialogFragment;

public class ReportStokFragment extends Fragment {
    private LaporanStokAdapter adapter;
    private ReportStokViewModel viewModel;
    private long startDate = 0;
    private long endDate = 0;
    private TextView tvTanggalDipilih;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report_stok, container, false);
        Button btnExportPdf = new Button(requireContext());
        btnExportPdf.setText("Export PDF");
        Button btnExportExcel = new Button(requireContext());
        btnExportExcel.setText("Export Excel");
        ((ViewGroup) view).addView(btnExportPdf, 0);
        ((ViewGroup) view).addView(btnExportExcel, 1);
        btnExportPdf.setOnClickListener(v -> exportPdf());
        btnExportExcel.setOnClickListener(v -> exportExcel());

        RecyclerView rv = view.findViewById(R.id.rvLaporanStok);
        adapter = new LaporanStokAdapter();
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())).get(ReportStokViewModel.class);
        viewModel.getLaporanStok().observe(getViewLifecycleOwner(), adapter::setData);

        tvTanggalDipilih = view.findViewById(R.id.tvTanggalDipilih);
        Button btnFilterTanggal = view.findViewById(R.id.btnFilterTanggal);
        btnFilterTanggal.setOnClickListener(v -> showDateRangePicker());

        Button btnTambahStokMasuk = view.findViewById(R.id.btnTambahStokMasuk);
        btnTambahStokMasuk.setOnClickListener(v -> {
            new StockInDialogFragment().show(getParentFragmentManager(), "stock_in_dialog");
        });

        // Load data awal (semua tanggal)
        startDate = 0;
        endDate = System.currentTimeMillis();
        loadLaporan();
        return view;
    }
    private void loadLaporan() {
        viewModel.loadLaporanStok(startDate, endDate);
    }
    private void showDateRangePicker() {
        MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Pilih Rentang Tanggal");
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            startDate = selection.first;
            endDate = selection.second;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String tgl = sdf.format(new Date(startDate)) + " - " + sdf.format(new Date(endDate));
            tvTanggalDipilih.setText(tgl);
            loadLaporan();
        });
        picker.show(getParentFragmentManager(), "date_range_picker");
    }
    private void exportPdf() {
        Toast.makeText(requireContext(), "Export PDF belum diimplementasikan", Toast.LENGTH_SHORT).show();
    }
    private void exportExcel() {
        Toast.makeText(requireContext(), "Export Excel belum diimplementasikan", Toast.LENGTH_SHORT).show();
    }
} 