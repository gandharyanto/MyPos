package id.tugas.pos.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import id.tugas.pos.data.model.StockIn;
import id.tugas.pos.data.repository.StockInRepository;

public class StockInViewModel extends AndroidViewModel {
    private final StockInRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public StockInViewModel(@NonNull Application application) {
        super(application);
        repository = new StockInRepository(application);
    }

    public void insert(StockIn stockIn, Runnable onSuccess) {
        executor.execute(() -> {
            repository.insert(stockIn);
            if (onSuccess != null) onSuccess.run();
        });
    }
} 