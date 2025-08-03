package id.tugas.pos.data.repository;

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
        PosDatabase database = PosDatabase.getInstance(application);
        productDao = database.productDao();
        allProducts = productDao.getAllProducts();
        executorService = Executors.newSingleThreadExecutor();
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
    
    public void decreaseStock(int productId, int quantity, OnProductOperationListener listener) {
        executorService.execute(() -> {
            try {
                productDao.decreaseStock(productId, quantity);
                listener.onSuccess();
            } catch (Exception e) {
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
        return productDao.getAllActiveProductsByStore(storeId);
    }
    
    public LiveData<List<String>> getAllCategoriesByStore(int storeId) {
        return productDao.getAllCategoriesByStore(storeId);
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
        executorService.execute(() -> {
            try {
                productDao.insert(product);
                listener.onSuccess();
            } catch (Exception e) {
                listener.onError(e.getMessage());
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
    
    public void getProductById(int id, OnProductSearchListener listener) {
        executorService.execute(() -> {
            try {
                Product product = productDao.getProductById(id).getValue();
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