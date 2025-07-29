package id.tugas.pos.ui.report;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import id.tugas.pos.data.repository.SavingRepository;
import java.util.List;

public class ReportSavingViewModel extends AndroidViewModel {
    private final SavingRepository repository;
    private MutableLiveData<List<LaporanSavingItem>> laporanSaving = new MutableLiveData<>(new java.util.ArrayList<>());

    public ReportSavingViewModel(@NonNull Application application) {
        super(application);
        repository = new SavingRepository(application);
    }

    public LiveData<List<LaporanSavingItem>> getLaporanSaving() {
        return laporanSaving;
    }

    public void loadLaporanSaving(long startDate, long endDate) {
        repository.getLaporanSaving(startDate, endDate).observeForever(data -> {
            laporanSaving.postValue(data);
        });
    }
    
    public void loadLaporanSavingByStore(long startDate, long endDate, int storeId) {
        repository.getLaporanSavingByStore(startDate, endDate, storeId).observeForever(data -> {
            laporanSaving.postValue(data);
        });
    }
} 