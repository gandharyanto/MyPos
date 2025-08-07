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
import id.tugas.pos.data.database.TransactionItemDao;
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
            int calculatedStock = totalStockIn - totalStockOut;
            
            // Only update stock if we have stock-in data or if calculated stock is positive
            // This prevents setting stock to 0 when there's no stock-in data
            if (totalStockIn > 0 || calculatedStock > 0) {
                productDao.updateStock(product.getId(), calculatedStock);
                Log.d(TAG, "Updated stock for product " + product.getName() + 
                      ": StockIn=" + totalStockIn + ", StockOut=" + totalStockOut + 
                      ", UpdatedStock=" + calculatedStock);
            } else {
                // Keep existing stock if no stock-in data and calculated stock would be 0 or negative
                Log.d(TAG, "Skipped stock update for product " + product.getName() + 
                      " (no stock-in data): StockIn=" + totalStockIn + ", StockOut=" + totalStockOut + 
                      ", KeepingCurrentStock=" + product.getStock());
            }
        }
    }
    
    private void syncStockInForStore(int storeId) {
        // This method can be extended to sync stock in data
        // For now, we just log the sync
        List<StockIn> stockInList = stockInDao.getAllStockInByStore(storeId);
        Log.d(TAG, "Synced " + stockInList.size() + " stock in records for store " + storeId);
    }
    
    private int getTotalStockOutForProduct(int productId) {
        // Calculate total stock out from transaction items
        try {
            // Get TransactionItemDao from database
            TransactionItemDao transactionItemDao = database.transactionItemDao();
            
            // Query to get total quantity sold for this product
            // We need to add this method to TransactionItemDao
            int totalStockOut = transactionItemDao.getTotalQuantitySoldForProduct(productId);
            
            Log.d(TAG, "Total stock out for product " + productId + ": " + totalStockOut);
            return totalStockOut;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating stock out for product " + productId + ": " + e.getMessage());
            return 0;
        }
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