package id.tugas.pos.ui.report;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import id.tugas.pos.data.repository.ExpenseRepository;
import java.util.List;

public class ReportPengeluaranViewModel extends AndroidViewModel {
    private final ExpenseRepository repository;
    private final MutableLiveData<List<LaporanPengeluaranItem>> laporanPengeluaran = new MutableLiveData<>();
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
        if (storeId != null && storeId > 0) {
            repository.getLaporanPengeluaranByStore(startDate, endDate, storeId).observeForever(data -> {
                laporanPengeluaran.postValue(data);
            });
        } else {
            repository.getLaporanPengeluaran(startDate, endDate).observeForever(data -> {
                laporanPengeluaran.postValue(data);
            });
        }
    }
}
