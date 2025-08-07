package id.tugas.pos.ui.produk;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import id.tugas.pos.data.model.Product;
import id.tugas.pos.data.repository.ProductRepository;

public class ProdukViewModel extends AndroidViewModel {

    private ProductRepository repository;
    private LiveData<List<Product>> allProducts;
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public ProdukViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
        allProducts = repository.getAllProducts();
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void addProduct(Product product) {
        android.util.Log.d("ProdukViewModel", "addProduct() called for product: " + product.getName());
        isLoading.setValue(true);
        repository.addProduct(product, new ProductRepository.OnProductOperationListener() {
            @Override
            public void onSuccess() {
                android.util.Log.d("ProdukViewModel", "Product added successfully in repository");
                mainHandler.post(() -> {
                    isLoading.setValue(false);
                    android.util.Log.d("ProdukViewModel", "Loading state set to false");
                });
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("ProdukViewModel", "Error adding product: " + error);
                mainHandler.post(() -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                    android.util.Log.e("ProdukViewModel", "Error message set: " + error);
                });
            }
        });
    }

    public void updateProduct(Product product) {
        isLoading.setValue(true);
        errorMessage.setValue(null); // Clear previous errors
        
        try {
            repository.updateProduct(product, new ProductRepository.OnProductOperationListener() {
                @Override
                public void onSuccess() {
                    mainHandler.post(() -> {
                        isLoading.setValue(false);
                        android.util.Log.d("ProdukViewModel", "Product updated successfully: " + product.getName());
                    });
                }

                @Override
                public void onError(String error) {
                    mainHandler.post(() -> {
                        isLoading.setValue(false);
                        errorMessage.setValue("Gagal memperbarui produk: " + error);
                        android.util.Log.e("ProdukViewModel", "Error updating product: " + error);
                    });
                }
            });
        } catch (Exception e) {
            mainHandler.post(() -> {
                isLoading.setValue(false);
                errorMessage.setValue("Terjadi kesalahan: " + e.getMessage());
                android.util.Log.e("ProdukViewModel", "Exception updating product: " + e.getMessage());
            });
        }
    }

    public void deleteProduct(Product product) {
        isLoading.setValue(true);
        repository.deleteProduct(product, new ProductRepository.OnProductOperationListener() {
            @Override
            public void onSuccess() {
                mainHandler.post(() -> {
                    isLoading.setValue(false);
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                });
            }
        });
    }

    public void searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            // Return all products if search is empty
            return;
        }
        
        repository.searchProducts(query, new ProductRepository.OnProductSearchListener() {
            @Override
            public void onSuccess(List<Product> products) {
                // Handle search results if needed
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    errorMessage.setValue(error);
                });
            }
        });
    }

    public void getProductByCode(String code) {
        repository.getProductByCode(code, new ProductRepository.OnProductSearchListener() {
            @Override
            public void onSuccess(List<Product> products) {
                // Handle single product result
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    errorMessage.setValue(error);
                });
            }
        });
    }

    public LiveData<List<Product>> getAllProductsByStore(int storeId) {
        return repository.getAllProductsByStore(storeId);
    }
    
    // Method to refresh product data (for UI refresh after transactions)
    public void refreshProductData() {
        android.util.Log.d("ProdukViewModel", "refreshProductData: Refreshing product data");
        repository.refreshAllProducts();
    }
}