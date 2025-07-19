package id.tugas.pos.ui.report;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import id.tugas.pos.data.repository.StokRepository;
import java.util.List;

public class ReportStokViewModel extends AndroidViewModel {
    private final StokRepository repository;
    private MutableLiveData<List<LaporanStokItem>> laporanStok = new MutableLiveData<>(new java.util.ArrayList<>());

    public ReportStokViewModel(@NonNull Application application) {
        super(application);
        repository = new StokRepository(application);
    }

    public LiveData<List<LaporanStokItem>> getLaporanStok() {
        return laporanStok;
    }

    public void loadLaporanStok(long startDate, long endDate) {
        repository.getLaporanStok(startDate, endDate).observeForever(data -> {
            laporanStok.postValue(data);
        });
    }
} 