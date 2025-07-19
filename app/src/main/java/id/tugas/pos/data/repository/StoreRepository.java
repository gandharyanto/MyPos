package id.tugas.pos.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import id.tugas.pos.data.database.PosDatabase;
import id.tugas.pos.data.database.StoreDao;
import id.tugas.pos.data.model.Store;

public class StoreRepository {
    private StoreDao storeDao;
    private LiveData<List<Store>> allStores;

    public StoreRepository(Application application) {
        PosDatabase database = PosDatabase.getInstance(application);
        storeDao = database.storeDao();
        allStores = storeDao.getAllStores();
    }

    public LiveData<List<Store>> getAllStores() {
        return allStores;
    }

    public LiveData<Store> getStoreById(int id) {
        return storeDao.getStoreById(id);
    }

    public void insert(Store store) {
        new Thread(() -> storeDao.insert(store)).start();
    }

    public void update(Store store) {
        new Thread(() -> storeDao.update(store)).start();
    }

    public void delete(Store store) {
        new Thread(() -> storeDao.delete(store)).start();
    }

    public void addStore(Store store, OnStoreOperationListener listener) {
        new Thread(() -> {
            try {
                storeDao.insert(store);
                if (listener != null) {
                    listener.onSuccess();
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError(e.getMessage());
                }
            }
        }).start();
    }

    public interface OnStoreOperationListener {
        void onSuccess();
        void onError(String message);
    }
} 