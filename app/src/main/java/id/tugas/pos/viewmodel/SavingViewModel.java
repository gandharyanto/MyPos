package id.tugas.pos.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import id.tugas.pos.data.model.Saving;
import id.tugas.pos.data.repository.SavingRepository;

public class SavingViewModel extends AndroidViewModel {
    private final SavingRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SavingViewModel(@NonNull Application application) {
        super(application);
        repository = new SavingRepository(application);
    }

    public void insert(Saving saving, Runnable onSuccess) {
        executor.execute(() -> {
            repository.insert(saving);
            if (onSuccess != null) onSuccess.run();
        });
    }
} 