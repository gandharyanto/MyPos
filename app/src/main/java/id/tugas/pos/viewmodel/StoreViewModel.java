package id.tugas.pos.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import id.tugas.pos.data.model.Store;
import id.tugas.pos.data.repository.StoreRepository;

public class StoreViewModel extends AndroidViewModel {
    private StoreRepository repository;
    private LiveData<List<Store>> allStores;
    private MutableLiveData<Integer> selectedStoreId = new MutableLiveData<>();

    public StoreViewModel(@NonNull Application application) {
        super(application);
        repository = new StoreRepository(application);
        allStores = repository.getAllStores();
    }

    public LiveData<List<Store>> getAllStores() {
        return allStores;
    }

    public LiveData<Integer> getSelectedStoreId() {
        return selectedStoreId;
    }

    public void setSelectedStoreId(int storeId) {
        selectedStoreId.setValue(storeId);
    }

    public void addStore(Store store) {
        repository.addStore(store, new StoreRepository.OnStoreOperationListener() {
            @Override
            public void onSuccess() {
                // Toko berhasil ditambahkan
            }

            @Override
            public void onError(String message) {
                // Handle error jika diperlukan
            }
        });
    }
} 