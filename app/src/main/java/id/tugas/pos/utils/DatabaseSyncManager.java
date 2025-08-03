package id.tugas.pos.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import id.tugas.pos.data.database.PosDatabase;
import id.tugas.pos.data.database.ProductDao;
import id.tugas.pos.data.database.StockInDao;
import id.tugas.pos.data.model.Product;
import id.tugas.pos.data.model.StockIn;
import java.util.List;

public class DatabaseSyncManager {
    private static final String TAG = "DatabaseSyncManager";
    private static final String PREFS_NAME = "sync_prefs";
    private static final String KEY_LAST_SYNC = "last_sync_timestamp";
    private static final String KEY_DEVICE_ID = "device_id";
    
    private Context context;
    private PosDatabase database;
    private ProductDao productDao;
    private StockInDao stockInDao;
    private ExecutorService executor;
    private MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);
    private MutableLiveData<String> syncStatus = new MutableLiveData<>("");
    
    public DatabaseSyncManager(Context context) {
        this.context = context;
        this.database = PosDatabase.getInstance(context);
        this.productDao = database.productDao();
        this.stockInDao = database.stockInDao();
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public void syncDataForStore(int storeId) {
        isSyncing.setValue(true);
        syncStatus.setValue("Memulai sinkronisasi data...");
        
        executor.execute(() -> {
            try {
                // Sync products for the store
                syncProductsForStore(storeId);
                
                // Sync stock in data for the store
                syncStockInForStore(storeId);
                
                // Update last sync timestamp
                updateLastSyncTimestamp();
                
                syncStatus.postValue("Sinkronisasi berhasil");
                isSyncing.postValue(false);
                
            } catch (Exception e) {
                Log.e(TAG, "Error during sync: " + e.getMessage());
                syncStatus.postValue("Error: " + e.getMessage());
                isSyncing.postValue(false);
            }
        });
    }
    
    private void syncProductsForStore(int storeId) {
        // Get all products for the store
        List<Product> storeProducts = productDao.getAllActiveProductsByStoreSync(storeId);
        
        // Update stock information based on stock in records
        for (Product product : storeProducts) {
            int totalStockIn = stockInDao.getTotalStockInForProduct(product.getId());
            int totalStockOut = getTotalStockOutForProduct(product.getId());
            
            // Calculate current stock
            int currentStock = totalStockIn - totalStockOut;
            
            // Update product stock
            productDao.updateStock(product.getId(), currentStock);
            
            Log.d(TAG, "Updated stock for product " + product.getName() + 
                  ": StockIn=" + totalStockIn + ", StockOut=" + totalStockOut + 
                  ", CurrentStock=" + currentStock);
        }
    }
    
    private void syncStockInForStore(int storeId) {
        // This method can be extended to sync stock in data
        // For now, we just log the sync
        List<StockIn> stockInList = stockInDao.getAllStockInByStore(storeId);
        Log.d(TAG, "Synced " + stockInList.size() + " stock in records for store " + storeId);
    }
    
    private int getTotalStockOutForProduct(int productId) {
        // This should calculate total stock out from transactions
        // For now, return 0 as placeholder
        return 0;
    }
    
    private void updateLastSyncTimestamp() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply();
    }
    
    public long getLastSyncTimestamp() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_LAST_SYNC, 0);
    }
    
    public String getDeviceId() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String deviceId = prefs.getString(KEY_DEVICE_ID, null);
        if (deviceId == null) {
            deviceId = "device_" + System.currentTimeMillis();
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
        }
        return deviceId;
    }
    
    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }
    
    public LiveData<String> getSyncStatus() {
        return syncStatus;
    }
    
    public void forceSyncForStore(int storeId) {
        Log.d(TAG, "Force sync requested for store " + storeId);
        syncDataForStore(storeId);
    }
    
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
} 