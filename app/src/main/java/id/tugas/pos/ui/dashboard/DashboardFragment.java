package id.tugas.pos.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import id.tugas.pos.R;

public class DashboardFragment extends Fragment {
    
    private DashboardViewModel dashboardViewModel;
    private TextView tvTotalRevenue, tvTodaySales, tvTotalProducts, tvLowStockCount;
    private TextView tvPendingTransactions, tvTotalExpenses, tvProfitMargin;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        
        // Initialize views
        initViews(view);
        
        // Observe data
        observeViewModel();
        
        // Load dashboard data
        dashboardViewModel.loadDashboardData();
    }
    
    private void initViews(View view) {
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvTodaySales = view.findViewById(R.id.tvTodaySales);
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts);
        tvLowStockCount = view.findViewById(R.id.tvLowStockCount);
        tvPendingTransactions = view.findViewById(R.id.tvPendingTransactions);
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses);
        tvProfitMargin = view.findViewById(R.id.tvProfitMargin);
    }
    
    private void observeViewModel() {
        // Observe total revenue
        dashboardViewModel.getTotalRevenue().observe(getViewLifecycleOwner(), revenue -> {
            if (revenue != null) {
                tvTotalRevenue.setText(id.tugas.pos.utils.CurrencyUtils.formatCurrency(revenue));
            }
        });
        
        // Observe today's sales
        dashboardViewModel.getTodaySales().observe(getViewLifecycleOwner(), sales -> {
            if (sales != null) {
                tvTodaySales.setText(id.tugas.pos.utils.CurrencyUtils.formatCurrency(sales));
            }
        });
        
        // Observe total products
        dashboardViewModel.getTotalProducts().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                tvTotalProducts.setText(String.valueOf(count));
            }
        });
        
        // Observe low stock count
        dashboardViewModel.getLowStockCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                tvLowStockCount.setText(String.valueOf(count));
            }
        });
        
        // Observe pending transactions
        dashboardViewModel.getPendingTransactions().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                tvPendingTransactions.setText(String.valueOf(count));
            }
        });
        
        // Observe total expenses
        dashboardViewModel.getTotalExpenses().observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null) {
                tvTotalExpenses.setText(id.tugas.pos.utils.CurrencyUtils.formatCurrency(expenses));
            }
        });
        
        // Observe profit margin
        dashboardViewModel.getProfitMargin().observe(getViewLifecycleOwner(), margin -> {
            if (margin != null) {
                tvProfitMargin.setText(id.tugas.pos.utils.CurrencyUtils.formatPercentage(margin));
            }
        });
    }
} 