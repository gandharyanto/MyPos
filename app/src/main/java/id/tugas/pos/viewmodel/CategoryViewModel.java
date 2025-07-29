package id.tugas.pos.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;

import id.tugas.pos.data.model.Category;
import id.tugas.pos.data.repository.CategoryRepository;

public class CategoryViewModel extends AndroidViewModel {
    private CategoryRepository repository;
    private MutableLiveData<Integer> selectedStoreId = new MutableLiveData<>();

    public CategoryViewModel(Application application) {
        super(application);
        repository = new CategoryRepository(application);
    }

    public void setSelectedStoreId(int storeId) {
        selectedStoreId.setValue(storeId);
    }

    public LiveData<Integer> getSelectedStoreId() {
        return selectedStoreId;
    }

    public void insert(Category category) {
        repository.insert(category, success -> {
            // Handle success/failure if needed
        });
    }

    public void update(Category category) {
        repository.update(category, success -> {
            // Handle success/failure if needed
        });
    }

    public void delete(Category category) {
        repository.delete(category, success -> {
            // Handle success/failure if needed
        });
    }

    public void softDelete(int categoryId) {
        repository.softDelete(categoryId, success -> {
            // Handle success/failure if needed
        });
    }

    public LiveData<Category> getCategoryById(int id) {
        return repository.getCategoryById(id);
    }

    public LiveData<Category> getCategoryByName(String name, int storeId) {
        return repository.getCategoryByName(name, storeId);
    }

    public LiveData<List<Category>> getAllActiveCategories() {
        return repository.getAllActiveCategories();
    }

    public LiveData<List<Category>> getAllActiveCategoriesByStore(int storeId) {
        return repository.getAllActiveCategoriesByStore(storeId);
    }

    public LiveData<List<String>> getAllCategoryNamesByStore(int storeId) {
        return repository.getAllCategoryNamesByStore(storeId);
    }

    public LiveData<Integer> getCategoryCountByStore(int storeId) {
        return repository.getCategoryCountByStore(storeId);
    }

    public LiveData<List<Category>> searchCategories(String searchQuery) {
        return repository.searchCategories(searchQuery);
    }

    public LiveData<List<Category>> searchCategoriesByStore(String searchQuery, int storeId) {
        return repository.searchCategoriesByStore(searchQuery, storeId);
    }

    public LiveData<Integer> getProductCountByCategory(String categoryName, int storeId) {
        return repository.getProductCountByCategory(categoryName, storeId);
    }
} 