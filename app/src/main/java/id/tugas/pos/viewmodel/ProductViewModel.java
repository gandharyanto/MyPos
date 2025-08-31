package id.tugas.pos.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import id.tugas.pos.data.model.Product;
import id.tugas.pos.data.repository.ProductRepository;

public class ProductViewModel extends AndroidViewModel {
    private final ProductRepository repository;

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

    public void clearData() {
        // No LiveData fields to reset in this ViewModel currently.
        // If you add LiveData fields (e.g., for filtered products), reset them here.
        // No repository cache to clear.
    }
}
