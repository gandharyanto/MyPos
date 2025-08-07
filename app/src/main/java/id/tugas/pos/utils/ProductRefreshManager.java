package id.tugas.pos.utils;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class to manage product data refresh across the application
 * This ensures that all product-related UI components are notified when product data changes
 */
public class ProductRefreshManager {
    
    private static final String TAG = "ProductRefreshManager";
    private static ProductRefreshManager instance;
    private List<ProductRefreshListener> listeners = new ArrayList<>();
    
    private ProductRefreshManager() {}
    
    public static synchronized ProductRefreshManager getInstance() {
        if (instance == null) {
            instance = new ProductRefreshManager();
        }
        return instance;
    }
    
    /**
     * Interface for components that need to be notified of product data changes
     */
    public interface ProductRefreshListener {
        void onProductDataChanged();
    }
    
    /**
     * Register a listener to be notified of product data changes
     */
    public void addListener(ProductRefreshListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            Log.d(TAG, "addListener: Registered listener, total listeners: " + listeners.size());
        }
    }
    
    /**
     * Unregister a listener
     */
    public void removeListener(ProductRefreshListener listener) {
        if (listener != null) {
            listeners.remove(listener);
            Log.d(TAG, "removeListener: Unregistered listener, total listeners: " + listeners.size());
        }
    }
    
    /**
     * Notify all registered listeners that product data has changed
     * This should be called after transactions, stock updates, product modifications, etc.
     */
    public void notifyProductDataChanged() {
        Log.d(TAG, "notifyProductDataChanged: Notifying " + listeners.size() + " listeners");
        
        // Create a copy of the list to avoid concurrent modification issues
        List<ProductRefreshListener> currentListeners = new ArrayList<>(listeners);
        
        for (ProductRefreshListener listener : currentListeners) {
            try {
                listener.onProductDataChanged();
            } catch (Exception e) {
                Log.e(TAG, "notifyProductDataChanged: Error notifying listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Clear all listeners (useful for cleanup)
     */
    public void clearListeners() {
        Log.d(TAG, "clearListeners: Clearing all listeners");
        listeners.clear();
    }
}
