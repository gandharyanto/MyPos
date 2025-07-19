package id.tugas.pos.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import id.tugas.pos.data.database.PosDatabase;
import id.tugas.pos.data.database.SavingDao;
import id.tugas.pos.data.model.Saving;
import id.tugas.pos.ui.report.LaporanSavingItem;

public class SavingRepository {
    private SavingDao savingDao;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public SavingRepository(Application application) {
        PosDatabase database = PosDatabase.getInstance(application);
        savingDao = database.savingDao();
    }

    public LiveData<List<LaporanSavingItem>> getLaporanSaving(long startDate, long endDate) {
        MutableLiveData<List<LaporanSavingItem>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<LaporanSavingItem> data = savingDao.getLaporanSaving(startDate, endDate);
            liveData.postValue(data);
        });
        return liveData;
    }

    public void insert(Saving saving) {
        executorService.execute(() -> {
            savingDao.insert(saving);
        });
    }
} 