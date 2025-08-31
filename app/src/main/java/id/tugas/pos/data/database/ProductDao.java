package id.tugas.pos.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import id.tugas.pos.data.model.Product;
import id.tugas.pos.ui.report.LaporanStokItem;

@Dao
public interface ProductDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Product product);
    
    @Update
    void update(Product product);
    
    @Delete
    void delete(Product product);
    
    @Query("SELECT * FROM products WHERE id = :id")
    LiveData<Product> getProductById(int id);
    
    @Query("SELECT * FROM products WHERE id = :id AND isActive = 1")
    Product getProductByIdSync(int id);
    
    @Query("SELECT * FROM products WHERE id = :id")
    Product getProductByIdAny(int id);
    
    @Query("SELECT * FROM products WHERE barcode = :barcode AND isActive = 1")
    LiveData<Product> getProductByBarcode(String barcode);
    
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    LiveData<List<Product>> getAllActiveProducts();
    
    @Query("SELECT * FROM products WHERE category = :category AND isActive = 1 ORDER BY name ASC")
    LiveData<List<Product>> getProductsByCategory(String category);
    
    @Query("SELECT * FROM products WHERE name LIKE '%' || :searchQuery || '%' AND isActive = 1 ORDER BY name ASC")
    LiveData<List<Product>> searchProducts(String searchQuery);
    
    @Query("SELECT * FROM products WHERE stock <= minStock AND isActive = 1 ORDER BY stock ASC")
    LiveData<List<Product>> getLowStockProducts();
    
    @Query("SELECT * FROM products WHERE stock = 0 AND isActive = 1 ORDER BY name ASC")
    LiveData<List<Product>> getOutOfStockProducts();
    
    @Query("SELECT DISTINCT category FROM products WHERE isActive = 1 ORDER BY category ASC")
    LiveData<List<String>> getAllCategories();
    
    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    LiveData<Integer> getActiveProductCount();
    
    @Query("SELECT COUNT(*) FROM products WHERE stock <= minStock AND isActive = 1")
    LiveData<Integer> getLowStockCount();
    
    @Query("UPDATE products SET stock = stock - :quantity WHERE id = :productId")
    void decreaseStock(int productId, int quantity);
    
    @Query("UPDATE products SET stock = stock - :quantity WHERE id = :productId")
    int decreaseStockWithReturn(int productId, int quantity);
    
    @Query("UPDATE products SET stock = stock + :quantity WHERE id = :productId")
    void increaseStock(int productId, int quantity);
    
    @Query("SELECT * FROM products WHERE isActive = 1")
    List<Product> getAllActiveProductsSync();
    
    // Additional methods for repository compatibility
    @Query("SELECT * FROM products ORDER BY name ASC")
    LiveData<List<Product>> getAllProducts();
    
    @Query("UPDATE products SET stock = :newStock WHERE id = :productId")
    void updateStock(int productId, int newStock);
    
    @Query("SELECT COUNT(*) FROM products")
    LiveData<Integer> getTotalCount();
    
    @Query("SELECT SUM(price * stock) FROM products WHERE isActive = 1")
    LiveData<Double> getTotalValue();

    @Query("SELECT * FROM products WHERE isActive = 1 AND storeId = :storeId ORDER BY name ASC")
    LiveData<List<Product>> getAllActiveProductsByStore(int storeId);
    
    @Query("SELECT * FROM products WHERE isActive = 1 AND storeId = :storeId ORDER BY name ASC")
    List<Product> getAllActiveProductsByStoreSync(int storeId);
    
    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1 AND storeId = :storeId")
    LiveData<Integer> getActiveProductCountByStore(int storeId);
    
    @Query("SELECT COUNT(*) FROM products WHERE stock <= minStock AND isActive = 1 AND storeId = :storeId")
    LiveData<Integer> getLowStockCountByStore(int storeId);
    
    @Query("SELECT DISTINCT category FROM products WHERE isActive = 1 AND storeId = :storeId ORDER BY category ASC")
    LiveData<List<String>> getAllCategoriesByStore(int storeId);

    @Query("SELECT name as namaProduk, 0 as stokMasuk, 0 as stokKeluar, stock as stokTersisa FROM products WHERE isActive = 1 ORDER BY name ASC")
    List<LaporanStokItem> getLaporanStokTersisa();
    
    @Query("SELECT name as namaProduk, 0 as stokMasuk, 0 as stokKeluar, stock as stokTersisa FROM products WHERE isActive = 1 AND storeId = :storeId ORDER BY name ASC")
    List<LaporanStokItem> getLaporanStokTersisaByStore(int storeId);
    
    // Method to force refresh of product data (triggers LiveData observers)
    // Update all products to ensure all LiveData observers are notified
    @Query("UPDATE products SET updatedAt = :timestamp WHERE isActive = 1")
    void refreshProductData(long timestamp);
    
    // Overloaded method with current timestamp
    default void refreshProductData() {
        refreshProductData(System.currentTimeMillis());
    }

    @Query("SELECT * FROM products WHERE storeId = :storeId AND isActive = 1 ORDER BY name ASC")
    LiveData<List<Product>> getAllProductsByStore(int storeId);
}
