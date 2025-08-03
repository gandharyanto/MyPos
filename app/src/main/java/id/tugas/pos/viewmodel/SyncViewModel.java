package id.tugas.pos.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import id.tugas.pos.utils.DatabaseSyncManager;
import id.tugas.pos.viewmodel.LoginViewModel;
import id.tugas.pos.data.model.User;

public class SyncViewModel extends AndroidViewModel {
    private DatabaseSyncManager syncManager;
    private LoginViewModel loginViewModel;
    private MutableLiveData<Boolean> shouldShowSyncButton = new MutableLiveData<>(false);
    
    public SyncViewModel(@NonNull Application application) {
        super(application);
        syncManager = new DatabaseSyncManager(application);
        loginViewModel = new LoginViewModel(application);
    }
    
    public void syncDataForCurrentUser() {
        User currentUser = loginViewModel.getCurrentUser().getValue();
        if (currentUser != null) {
            if (currentUser.isAdmin()) {
                // Admin can sync for any store, but we'll sync for the currently selected store
                // This will be handled by the calling fragment
                shouldShowSyncButton.setValue(true);
            } else {
                // User can only sync for their assigned store
                if (currentUser.getStoreId() != null) {
                    syncManager.syncDataForStore(currentUser.getStoreId());
                }
            }
        }
    }
    
    public void syncDataForStore(int storeId) {
        syncManager.syncDataForStore(storeId);
    }
    
    public void forceSyncForStore(int storeId) {
        syncManager.forceSyncForStore(storeId);
    }
    
    public LiveData<Boolean> getIsSyncing() {
        return syncManager.getIsSyncing();
    }
    
    public LiveData<String> getSyncStatus() {
        return syncManager.getSyncStatus();
    }
    
    public LiveData<Boolean> getShouldShowSyncButton() {
        return shouldShowSyncButton;
    }
    
    public long getLastSyncTimestamp() {
        return syncManager.getLastSyncTimestamp();
    }
    
    public String getDeviceId() {
        return syncManager.getDeviceId();
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        syncManager.cleanup();
    }
} 