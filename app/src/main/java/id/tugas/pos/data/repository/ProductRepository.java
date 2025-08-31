package id.tugas.pos.data.repository;

import android.util.Log;
import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;

import id.tugas.pos.data.database.PosDatabase;
import id.tugas.pos.data.database.ProductDao;
import id.tugas.pos.data.model.Product;

public class ProductRepository {
    
    private ProductDao productDao;
    private LiveData<List<Product>> allProducts;
    private ExecutorService executorService;
    
    public ProductRepository(Application application) {
        try {
            android.util.Log.d("ProductRepository", "Initializing ProductRepository");
            PosDatabase database = PosDatabase.getInstance(application);
            productDao = database.productDao();
            allProducts = productDao.getAllProducts();
            executorService = Executors.newSingleThreadExecutor();
            android.util.Log.d("ProductRepository", "ProductRepository initialized successfully");
        } catch (Exception e) {
            android.util.Log.e("ProductRepository", "Error initializing ProductRepository: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize ProductRepository", e);
        }
    }
    
    public LiveData<List<Product>> getAllProducts() {
        return productDao.getAllProducts();
    }

    public List<Product> getAllProductsSync() {
        try {
            return productDao.getAllActiveProductsSync();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public void insert(Product product) {
        executorService.execute(() -> {
            productDao.insert(product);
        });
    }
    
    public void update(Product product) {
        executorService.execute(() -> {
            productDao.update(product);
        });
    }
    
    public void delete(Product product) {
        executorService.execute(() -> {
            productDao.delete(product);
        });
    }
    
    public LiveData<Product> getProductById(int id) {
        return productDao.getProductById(id);
    }
    
    public LiveData<Product> getProductByBarcode(String barcode) {
        return productDao.getProductByBarcode(barcode);
    }
    
    public LiveData<List<Product>> searchProducts(String query) {
        return productDao.searchProducts("%" + query + "%");
    }
    
    public LiveData<List<Product>> getLowStockProducts() {
        return productDao.getLowStockProducts();
    }
    
    public void updateStock(int productId, int newStock) {
        executorService.execute(() -> {
            productDao.updateStock(productId, newStock);
        });
    }
    
    public void getProductById(int productId, OnProductSearchListener listener) {
        Log.d("ProductRepository", "getProductById: Searching for product with ID: " + productId);
        executorService.execute(() -> {
            try {
                // First check if product exists at all (without isActive filter)
                Product productAny = productDao.getProductByIdAny(productId);
                if (productAny != null) {
                    Log.d("ProductRepository", "getProductById: Product exists in DB - Name: " + productAny.getName() + ", Stock: " + productAny.getStock() + ", isActive: " + productAny.isActive());
                } else {
                    Log.w("ProductRepository", "getProductById: Product does not exist in DB with ID: " + productId);
                }
                
                // Then get product with isActive filter
                Product product = productDao.getProductByIdSync(productId);
                List<Product> products = new ArrayList<>();
                if (product != null) {
                    products.add(product);
                    Log.d("ProductRepository", "getProductById: Active product found - Name: " + product.getName() + ", Stock: " + product.getStock());
                } else {
                    Log.w("ProductRepository", "getProductById: Active product not found with ID: " + productId);
                }
                listener.onSuccess(products);
            } catch (Exception e) {
                Log.e("ProductRepository", "getProductById: Error: " + e.getMessage(), e);
                listener.onError(e.getMessage());
            }
        });
    }
    
    public void decreaseStock(int productId, int quantity, OnProductOperationListener listener) {
        Log.d("ProductRepository", "decreaseStock: Starting stock decrease for product ID: " + productId + ", quantity: " + quantity);
        executorService.execute(() -> {
            try {
                // Get product before update to log current stock
                Product productBefore = productDao.getProductByIdSync(productId);
                if (productBefore != null) {
                    Log.d("ProductRepository", "decreaseStock: Product before update - Name: " + productBefore.getName() + ", Stock: " + productBefore.getStock());
                } else {
                    Log.w("ProductRepository", "decreaseStock: Product not found with ID: " + productId);
                }
                
                // Perform the stock decrease
                int rowsAffected = productDao.decreaseStockWithReturn(productId, quantity);
                Log.d("ProductRepository", "decreaseStock: Rows affected: " + rowsAffected);
                
                if (rowsAffected > 0) {
                    // Get product after update to verify the change
                    Product productAfter = productDao.getProductByIdSync(productId);
                    if (productAfter != null) {
                        Log.d("ProductRepository", "decreaseStock: Product after update - Name: " + productAfter.getName() + ", Stock: " + productAfter.getStock());
                    }
                    Log.d("ProductRepository", "decreaseStock: Stock decreased successfully");
                    listener.onSuccess();
                } else {
                    Log.e("ProductRepository", "decreaseStock: No rows affected - stock not decreased");
                    listener.onError("No rows affected - stock not decreased");
                }
            } catch (Exception e) {
                Log.e("ProductRepository", "decreaseStock: Exception occurred: " + e.getMessage(), e);
                listener.onError(e.getMessage());
            }
        });
    }
    
    public void increaseStock(int productId, int quantity, OnProductOperationListener listener) {
        executorService.execute(() -> {
            try {
                productDao.increaseStock(productId, quantity);
                listener.onSuccess();
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        });
    }
    
    public LiveData<Integer> getTotalProductCount() {
        return productDao.getTotalCount();
    }
    
    public LiveData<Double> getTotalProductValue() {
        return productDao.getTotalValue();
    }
    
    public LiveData<List<Product>> getAllProductsByStore(int storeId) {
        return productDao.getAllProductsByStore(storeId);
    }
    
    public LiveData<List<String>> getAllCategoriesByStore(int storeId) {
        return productDao.getAllCategoriesByStore(storeId);
    }
    
    public void refreshAllProducts() {
        // Force refresh of all product data by invalidating cache
        android.util.Log.d("ProductRepository", "refreshAllProducts: Forcing product data refresh");
        // This will trigger observers to re-fetch data from database
        executorService.execute(() -> {
            try {
                // Force database to refresh by touching the data
                // This will cause LiveData observers to be notified
                productDao.refreshProductData();
                android.util.Log.d("ProductRepository", "refreshAllProducts: Product data refresh completed");
            } catch (Exception e) {
                android.util.Log.e("ProductRepository", "refreshAllProducts: Error refreshing product data: " + e.getMessage());
            }
        });
    }
    
    // Additional methods for ViewModel compatibility
    public LiveData<Integer> getActiveProductCount() {
        return productDao.getActiveProductCount();
    }
    
    public LiveData<Integer> getActiveProductCountByStore(Integer storeId) {
        if (storeId == null) {
            return productDao.getActiveProductCount();
        }
        return productDao.getActiveProductCountByStore(storeId);
    }
    
    public LiveData<Integer> getLowStockCount() {
        return productDao.getLowStockCount();
    }
    
    public LiveData<Integer> getLowStockCountByStore(Integer storeId) {
        if (storeId == null) {
            return productDao.getLowStockCount();
        }
        return productDao.getLowStockCountByStore(storeId);
    }
    
    // Callback interfaces
    public interface OnProductOperationListener {
        void onSuccess();
        void onError(String message);
    }
    
    public interface OnProductSearchListener {
        void onSuccess(List<Product> products);
        void onError(String message);
    }
    
    // Methods with callbacks
    public void addProduct(Product product, OnProductOperationListener listener) {
        android.util.Log.d("ProductRepository", "addProduct() called for product: " + product.getName());
        
        if (executorService == null || executorService.isShutdown()) {
            android.util.Log.e("ProductRepository", "ExecutorService is null or shutdown");
            listener.onError("Database service tidak tersedia");
            return;
        }
        
        executorService.execute(() -> {
            try {
                android.util.Log.d("ProductRepository", "Executing addProduct in background thread");
                
                // Validate product data before inserting
                if (product.getName() == null || product.getName().trim().isEmpty()) {
                    android.util.Log.e("ProductRepository", "Product name is null or empty");
                    listener.onError("Nama produk tidak boleh kosong");
                    return;
                }
                
                if (product.getBarcode() == null || product.getBarcode().trim().isEmpty()) {
                    android.util.Log.e("ProductRepository", "Product barcode is null or empty");
                    listener.onError("Kode produk tidak boleh kosong");
                    return;
                }
                
                if (product.getStock() < 0) {
                    android.util.Log.e("ProductRepository", "Product stock is negative");
                    listener.onError("Stok tidak boleh negatif");
                    return;
                }
                
                if (product.getPrice() < 0) {
                    android.util.Log.e("ProductRepository", "Product price is negative");
                    listener.onError("Harga tidak boleh negatif");
                    return;
                }
                
                android.util.Log.d("ProductRepository", "Product validation passed, inserting into database");
                
                // Check if database is accessible
                if (productDao == null) {
                    android.util.Log.e("ProductRepository", "ProductDao is null");
                    listener.onError("Database tidak tersedia");
                    return;
                }
                
                productDao.insert(product);
                android.util.Log.d("ProductRepository", "Product inserted successfully in database");
                listener.onSuccess();
            } catch (Exception e) {
                android.util.Log.e("ProductRepository", "Error adding product: " + e.getMessage(), e);
                listener.onError("Gagal menambahkan produk: " + e.getMessage());
            }
        });
    }
    
    public void updateProduct(Product product, OnProductOperationListener listener) {
        executorService.execute(() -> {
            try {
                android.util.Log.d("ProductRepository", "Updating product: " + product.getName() + " (ID: " + product.getId() + ")");
                
                // Validate product data
                if (product.getName() == null || product.getName().trim().isEmpty()) {
                    listener.onError("Nama produk tidak boleh kosong");
                    return;
                }
                
                if (product.getBarcode() == null || product.getBarcode().trim().isEmpty()) {
                    listener.onError("Kode produk tidak boleh kosong");
                    return;
                }
                
                if (product.getStock() < 0) {
                    listener.onError("Stok tidak boleh negatif");
                    return;
                }
                
                if (product.getPrice() < 0) {
                    listener.onError("Harga tidak boleh negatif");
                    return;
                }
                
                // Update the product
                productDao.update(product);
                android.util.Log.d("ProductRepository", "Product updated successfully: " + product.getName());
                listener.onSuccess();
                
            } catch (Exception e) {
                android.util.Log.e("ProductRepository", "Error updating product: " + e.getMessage());
                listener.onError("Gagal memperbarui produk: " + e.getMessage());
            }
        });
    }
    
    public void deleteProduct(Product product, OnProductOperationListener listener) {
        executorService.execute(() -> {
            try {
                productDao.delete(product);
                listener.onSuccess();
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        });
    }
    
    public void searchProducts(String query, OnProductSearchListener listener) {
        executorService.execute(() -> {
            try {
                List<Product> products = productDao.searchProducts("%" + query + "%").getValue();
                if (products != null) {
                    listener.onSuccess(products);
                } else {
                    listener.onSuccess(new ArrayList<>());
                }
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        });
    }
    
    public void getProductByCode(String code, OnProductSearchListener listener) {
        executorService.execute(() -> {
            try {
                Product product = productDao.getProductByBarcode(code).getValue();
                List<Product> products = new ArrayList<>();
                if (product != null) {
                    products.add(product);
                }
                listener.onSuccess(products);
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        });
    }
    
    public void getProductStock(int productId, OnProductSearchListener listener) {
        executorService.execute(() -> {
            try {
                Product product = productDao.getProductById(productId).getValue();
                List<Product> products = new ArrayList<>();
                if (product != null) {
                    products.add(product);
                    System.out.println("DEBUG: Product " + product.getName() + " (ID: " + productId + ") stock: " + product.getStock());
                }
                listener.onSuccess(products);
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        });
    }
}
