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
    private MutableLiveData<List<LaporanPengeluaranItem>> laporanPengeluaran = new MutableLiveData<>(new java.util.ArrayList<>());

    public ReportPengeluaranViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
    }

    public LiveData<List<LaporanPengeluaranItem>> getLaporanPengeluaran() {
        return laporanPengeluaran;
    }

    public void loadLaporanPengeluaran(long startDate, long endDate) {
        repository.getLaporanPengeluaran(startDate, endDate).observeForever(data -> {
            laporanPengeluaran.postValue(data);
        });
    }
} 