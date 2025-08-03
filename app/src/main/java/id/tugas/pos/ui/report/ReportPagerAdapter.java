package id.tugas.pos.ui.report;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ReportPagerAdapter extends FragmentStateAdapter {
    public ReportPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new ReportTransaksiFragment();
            case 1: return new ReportPengeluaranFragment();
            case 2: return new ReportStokFragment();
            default: return new ReportTransaksiFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
} 