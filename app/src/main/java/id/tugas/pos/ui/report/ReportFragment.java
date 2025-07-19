package id.tugas.pos.ui.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import id.tugas.pos.R;
import id.tugas.pos.ui.MainActivity;
import id.tugas.pos.viewmodel.LoginViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ReportFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ReportPagerAdapter pagerAdapter;
    private MainActivity mainActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayout = view.findViewById(R.id.tabLayoutReport);
        viewPager = view.findViewById(R.id.viewPagerReport);
        pagerAdapter = new ReportPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Transaksi"); break;
                case 1: tab.setText("Pengeluaran"); break;
                case 2: tab.setText("Saving"); break;
                case 3: tab.setText("Stok"); break;
            }
        }).attach();

        mainActivity = (MainActivity) requireActivity();
        LoginViewModel loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        if (loginViewModel.isAdmin()) {
            mainActivity.spinnerStore.setVisibility(View.VISIBLE);
            mainActivity.labelStore.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mainActivity != null) {
            mainActivity.spinnerStore.setVisibility(View.GONE);
            mainActivity.labelStore.setVisibility(View.GONE);
        }
    }
} 