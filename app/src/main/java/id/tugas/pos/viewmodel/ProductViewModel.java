package id.tugas.pos.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import id.tugas.pos.data.model.Product;
import id.tugas.pos.data.repository.ProductRepository;

public class ProductViewModel extends AndroidViewModel {
    
    private ProductRepository repository;
    private LiveData<List<Product>> allProducts;
    private MutableLiveData<List<Product>> searchResults;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;
    private ExecutorService executorService;
    
    public ProductViewModel(Application application) {
        super(application);
        repository = new ProductRepository(application);
        allProducts = repository.getAllProducts();
        searchResults = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }
    
    public LiveData<List<Product>> getSearchResults() {
        return searchResults;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public void insertProduct(Product product) {
        isLoading.setValue(true);
        executorService.execute(() -> {
            try {
                repository.insert(product);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Gagal menambah produk: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    public void updateProduct(Product product) {
        isLoading.setValue(true);
        executorService.execute(() -> {
            try {
                repository.update(product);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Gagal mengupdate produk: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    public void deleteProduct(Product product) {
        isLoading.setValue(true);
        executorService.execute(() -> {
            try {
                repository.delete(product);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Gagal menghapus produk: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    public void searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            searchResults.setValue(null);
            return;
        }
        
        isLoading.setValue(true);
        LiveData<List<Product>> searchLiveData = repository.searchProducts(query);
        searchLiveData.observeForever(products -> {
            searchResults.setValue(products);
            isLoading.setValue(false);
        });
    }
    
    public void getLowStockProducts() {
        isLoading.setValue(true);
        LiveData<List<Product>> lowStockLiveData = repository.getLowStockProducts();
        lowStockLiveData.observeForever(products -> {
            searchResults.setValue(products);
            isLoading.setValue(false);
        });
    }
    
    public void updateStock(int productId, int newStock) {
        isLoading.setValue(true);
        executorService.execute(() -> {
            try {
                repository.updateStock(productId, newStock);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Gagal mengupdate stok: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
} 