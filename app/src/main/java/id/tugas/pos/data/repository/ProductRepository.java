package id.tugas.pos.data.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import id.tugas.pos.data.database.PosDatabase;
import id.tugas.pos.data.database.ProductDao;
import id.tugas.pos.data.model.Product;

public class ProductRepository {
    
    private ProductDao productDao;
    private LiveData<List<Product>> allActiveProducts;
    private LiveData<Integer> activeProductCount;
    private LiveData<Integer> lowStockCount;
    private LiveData<List<String>> allCategories;
    
    public ProductRepository(Application application) {
        PosDatabase database = PosDatabase.getInstance(application);
        productDao = database.productDao();
        allActiveProducts = productDao.getAllActiveProducts();
        activeProductCount = productDao.getActiveProductCount();
        lowStockCount = productDao.getLowStockCount();
        allCategories = productDao.getAllCategories();
    }
    
    // Insert product
    public void insert(Product product) {
        new InsertProductAsyncTask(productDao).execute(product);
    }
    
    // Update product
    public void update(Product product) {
        new UpdateProductAsyncTask(productDao).execute(product);
    }
    
    // Delete product
    public void delete(Product product) {
        new DeleteProductAsyncTask(productDao).execute(product);
    }
    
    // Get product by ID
    public LiveData<Product> getProductById(int id) {
        return productDao.getProductById(id);
    }
    
    // Get product by barcode
    public LiveData<Product> getProductByBarcode(String barcode) {
        return productDao.getProductByBarcode(barcode);
    }
    
    // Get all active products
    public LiveData<List<Product>> getAllActiveProducts() {
        return allActiveProducts;
    }
    
    // Get products by category
    public LiveData<List<Product>> getProductsByCategory(String category) {
        return productDao.getProductsByCategory(category);
    }
    
    // Search products
    public LiveData<List<Product>> searchProducts(String searchQuery) {
        return productDao.searchProducts(searchQuery);
    }
    
    // Get low stock products
    public LiveData<List<Product>> getLowStockProducts() {
        return productDao.getLowStockProducts();
    }
    
    // Get out of stock products
    public LiveData<List<Product>> getOutOfStockProducts() {
        return productDao.getOutOfStockProducts();
    }
    
    // Get all categories
    public LiveData<List<String>> getAllCategories() {
        return allCategories;
    }
    
    // Get active product count
    public LiveData<Integer> getActiveProductCount() {
        return activeProductCount;
    }
    
    // Get low stock count
    public LiveData<Integer> getLowStockCount() {
        return lowStockCount;
    }
    
    // Decrease stock
    public void decreaseStock(int productId, int quantity) {
        new DecreaseStockAsyncTask(productDao).execute(productId, quantity);
    }
    
    // Increase stock
    public void increaseStock(int productId, int quantity) {
        new IncreaseStockAsyncTask(productDao).execute(productId, quantity);
    }
    
    // Get all active products synchronously
    public List<Product> getAllActiveProductsSync() {
        return productDao.getAllActiveProductsSync();
    }
    
    // AsyncTask classes
    private static class InsertProductAsyncTask extends AsyncTask<Product, Void, Void> {
        private ProductDao productDao;
        
        InsertProductAsyncTask(ProductDao productDao) {
            this.productDao = productDao;
        }
        
        @Override
        protected Void doInBackground(Product... products) {
            productDao.insert(products[0]);
            return null;
        }
    }
    
    private static class UpdateProductAsyncTask extends AsyncTask<Product, Void, Void> {
        private ProductDao productDao;
        
        UpdateProductAsyncTask(ProductDao productDao) {
            this.productDao = productDao;
        }
        
        @Override
        protected Void doInBackground(Product... products) {
            productDao.update(products[0]);
            return null;
        }
    }
    
    private static class DeleteProductAsyncTask extends AsyncTask<Product, Void, Void> {
        private ProductDao productDao;
        
        DeleteProductAsyncTask(ProductDao productDao) {
            this.productDao = productDao;
        }
        
        @Override
        protected Void doInBackground(Product... products) {
            productDao.delete(products[0]);
            return null;
        }
    }
    
    private static class DecreaseStockAsyncTask extends AsyncTask<Object, Void, Void> {
        private ProductDao productDao;
        
        DecreaseStockAsyncTask(ProductDao productDao) {
            this.productDao = productDao;
        }
        
        @Override
        protected Void doInBackground(Object... params) {
            int productId = (Integer) params[0];
            int quantity = (Integer) params[1];
            productDao.decreaseStock(productId, quantity);
            return null;
        }
    }
    
    private static class IncreaseStockAsyncTask extends AsyncTask<Object, Void, Void> {
        private ProductDao productDao;
        
        IncreaseStockAsyncTask(ProductDao productDao) {
            this.productDao = productDao;
        }
        
        @Override
        protected Void doInBackground(Object... params) {
            int productId = (Integer) params[0];
            int quantity = (Integer) params[1];
            productDao.increaseStock(productId, quantity);
            return null;
        }
    }
} 