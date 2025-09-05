package id.tugas.pos.ui.report;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import id.tugas.pos.data.repository.ExpenseRepository;
import java.util.List;

public class ReportPengeluaranViewModel extends AndroidViewModel {
    private final ExpenseRepository repository;
    private final MediatorLiveData<List<LaporanPengeluaranItem>> laporanPengeluaran = new MediatorLiveData<>();
    private LiveData<List<LaporanPengeluaranItem>> currentSource;
    private long startDate = 0;
    private long endDate = System.currentTimeMillis();
    private Integer storeId = null;

    public ReportPengeluaranViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
        updateLaporanPengeluaran();
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
        LiveData<List<LaporanPengeluaranItem>> newSource;
        if (storeId != null && storeId > 0) {
            newSource = repository.getLaporanPengeluaranByStore(startDate, endDate, storeId);
        } else {
            newSource = repository.getLaporanPengeluaran(startDate, endDate);
        }
        if (currentSource != null) {
            laporanPengeluaran.removeSource(currentSource);
        }
        currentSource = newSource;
        laporanPengeluaran.addSource(currentSource, laporanPengeluaran::setValue);
    }
}
