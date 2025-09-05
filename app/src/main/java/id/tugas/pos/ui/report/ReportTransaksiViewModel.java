package id.tugas.pos.ui.report;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import id.tugas.pos.data.repository.TransactionRepository;
import java.util.List;

public class ReportTransaksiViewModel extends AndroidViewModel {
    private final TransactionRepository repository;
    private final MutableLiveData<List<LaporanTransaksiItem>> laporanTransaksi = new MutableLiveData<>();

    public ReportTransaksiViewModel(@NonNull Application application) {
        super(application);
        repository = new TransactionRepository(application);
    }

    public LiveData<List<LaporanTransaksiItem>> getLaporanTransaksi() {
        return laporanTransaksi;
    }

    public void loadLaporanTransaksi(long startDate, long endDate) {
        repository.getLaporanTransaksi(startDate, endDate).observeForever(data -> {
            laporanTransaksi.postValue(data);
        });
    }
    
    public void loadLaporanTransaksiByStore(long startDate, long endDate, int storeId) {
        repository.getLaporanTransaksiByStore(startDate, endDate, storeId).observeForever(data -> {
            laporanTransaksi.postValue(data);
        });
    }
}
