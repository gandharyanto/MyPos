package id.tugas.pos.ui.riwayat;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import id.tugas.pos.R;
import id.tugas.pos.data.model.Transaction;
import id.tugas.pos.ui.riwayat.adapter.TransactionAdapter;
import id.tugas.pos.ui.riwayat.dialog.TransactionDetailDialog;
import id.tugas.pos.utils.CurrencyUtils;

public class RiwayatFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {

    private RiwayatViewModel viewModel;
    private TransactionAdapter adapter;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private TextView tvTotalTransactions, tvTotalRevenue;
    private MaterialButton btnFilterToday, btnFilterWeek, btnFilterMonth, btnFilterAll;
    private List<Transaction> allTransactions = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_riwayat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupSearchView();
        setupFilterButtons();
        observeData();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_transactions);
        searchView = view.findViewById(R.id.search_view_transactions);
        tvTotalTransactions = view.findViewById(R.id.tv_total_transactions);
        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue);
        btnFilterToday = view.findViewById(R.id.btn_filter_today);
        btnFilterWeek = view.findViewById(R.id.btn_filter_week);
        btnFilterMonth = view.findViewById(R.id.btn_filter_month);
        btnFilterAll = view.findViewById(R.id.btn_filter_all);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(RiwayatViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterTransactions(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTransactions(newText);
                return true;
            }
        });
    }

    private void setupFilterButtons() {
        btnFilterToday.setOnClickListener(v -> viewModel.filterByDate("today"));
        btnFilterWeek.setOnClickListener(v -> viewModel.filterByDate("week"));
        btnFilterMonth.setOnClickListener(v -> viewModel.filterByDate("month"));
        btnFilterAll.setOnClickListener(v -> viewModel.filterByDate("all"));
    }

    private void observeData() {
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            allTransactions = transactions;
            adapter.submitList(transactions);
            updateSummary(transactions);
        });

        viewModel.getFilteredTransactions().observe(getViewLifecycleOwner(), transactions -> {
            adapter.submitList(transactions);
            updateSummary(transactions);
        });
    }

    private void filterTransactions(String query) {
        List<Transaction> filteredList = new ArrayList<>();
        for (Transaction transaction : allTransactions) {
            if (String.valueOf(transaction.getId()).contains(query) ||
                transaction.getCashierName().toLowerCase().contains(query.toLowerCase()) ||
                transaction.getPaymentMethod().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(transaction);
            }
        }
        adapter.submitList(filteredList);
        updateSummary(filteredList);
    }

    private void updateSummary(List<Transaction> transactions) {
        int totalCount = transactions.size();
        double totalRevenue = 0;

        for (Transaction transaction : transactions) {
            totalRevenue += transaction.getTotalAmount();
        }

        tvTotalTransactions.setText("Total Transaksi: " + totalCount);
        tvTotalRevenue.setText("Total Pendapatan: " + CurrencyUtils.formatCurrency(totalRevenue));
    }

    private void showTransactionDetailDialog(Transaction transaction) {
        TransactionDetailDialog dialog = TransactionDetailDialog.newInstance(transaction.getId());
        dialog.show(getChildFragmentManager(), "TransactionDetailDialog");
    }

    private void showDeleteConfirmationDialog(Transaction transaction) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus Transaksi")
                .setMessage("Apakah Anda yakin ingin menghapus transaksi #" + transaction.getId() + "?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    viewModel.deleteTransaction(transaction);
                    Toast.makeText(requireContext(), "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        showTransactionDetailDialog(transaction);
    }

    @Override
    public void onTransactionLongClick(Transaction transaction) {
        showDeleteConfirmationDialog(transaction);
    }
} 