package id.tugas.pos.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import id.tugas.pos.data.model.Product;
import id.tugas.pos.data.repository.ProductRepository;

public class ProductViewModel extends AndroidViewModel {
    private final ProductRepository repository;
    private MutableLiveData<Integer> currentStoreId = new MutableLiveData<>();
    private LiveData<List<Product>> productsByStore;

    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
    }

    public LiveData<List<Product>> getAllProducts() {
        return repository.getAllProducts();
    }
    
    public LiveData<List<String>> getAllCategoriesByStore(int storeId) {
        return repository.getAllCategoriesByStore(storeId);
    }

    public LiveData<List<Product>> getProductsByStore() {
        return productsByStore;
    }

    public void setStoreId(int storeId) {
        currentStoreId.setValue(storeId);
        productsByStore = repository.getAllProductsByStore(storeId);
    }

    public void clearData() {
        currentStoreId.setValue(null);
        productsByStore = null;
    }
}
