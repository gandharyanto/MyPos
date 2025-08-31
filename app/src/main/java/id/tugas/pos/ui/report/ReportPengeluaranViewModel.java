package id.tugas.pos.ui.report;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import id.tugas.pos.data.repository.ExpenseRepository;
import java.util.List;

public class ReportPengeluaranViewModel extends AndroidViewModel {
    private final ExpenseRepository repository;
    private LiveData<List<LaporanPengeluaranItem>> laporanPengeluaran;
    private long startDate = 0;
    private long endDate = System.currentTimeMillis();
    private Integer storeId = null;

    public ReportPengeluaranViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
        // Default: all expenses
        laporanPengeluaran = repository.getLaporanPengeluaran(startDate, endDate);
    }

    public LiveData<List<LaporanPengeluaranItem>> getLaporanPengeluaran() {
        return laporanPengeluaran;
    }

    public void setDateRange(long start, long end) {
        this.startDate = start;
        this.endDate = end;
        updateLaporanPengeluaran();
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
        updateLaporanPengeluaran();
    }

    private void updateLaporanPengeluaran() {
        if (storeId != null && storeId > 0) {
            laporanPengeluaran = repository.getLaporanPengeluaranByStore(startDate, endDate, storeId);
        } else {
            laporanPengeluaran = repository.getLaporanPengeluaran(startDate, endDate);
        }
    }
}
