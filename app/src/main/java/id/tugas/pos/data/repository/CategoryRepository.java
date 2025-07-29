package id.tugas.pos.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import id.tugas.pos.data.database.CategoryDao;
import id.tugas.pos.data.database.PosDatabase;
import id.tugas.pos.data.model.Category;

public class CategoryRepository {
    private CategoryDao categoryDao;
    private ExecutorService executorService;

    public CategoryRepository(Context context) {
        PosDatabase database = PosDatabase.getInstance(context);
        categoryDao = database.categoryDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Category category, OnCompleteListener listener) {
        executorService.execute(() -> {
            long id = categoryDao.insert(category);
            if (listener != null) {
                listener.onComplete(id > 0);
            }
        });
    }

    public void update(Category category, OnCompleteListener listener) {
        executorService.execute(() -> {
            categoryDao.update(category);
            if (listener != null) {
                listener.onComplete(true);
            }
        });
    }

    public void delete(Category category, OnCompleteListener listener) {
        executorService.execute(() -> {
            categoryDao.delete(category);
            if (listener != null) {
                listener.onComplete(true);
            }
        });
    }

    public void softDelete(int categoryId, OnCompleteListener listener) {
        executorService.execute(() -> {
            categoryDao.softDeleteCategory(categoryId);
            if (listener != null) {
                listener.onComplete(true);
            }
        });
    }

    public LiveData<Category> getCategoryById(int id) {
        return categoryDao.getCategoryById(id);
    }

    public LiveData<Category> getCategoryByName(String name, int storeId) {
        return categoryDao.getCategoryByName(name, storeId);
    }

    public LiveData<List<Category>> getAllActiveCategories() {
        return categoryDao.getAllActiveCategories();
    }

    public LiveData<List<Category>> getAllActiveCategoriesByStore(int storeId) {
        return categoryDao.getAllActiveCategoriesByStore(storeId);
    }

    public LiveData<List<String>> getAllCategoryNamesByStore(int storeId) {
        return categoryDao.getAllCategoryNamesByStore(storeId);
    }

    public LiveData<Integer> getCategoryCountByStore(int storeId) {
        return categoryDao.getCategoryCountByStore(storeId);
    }

    public LiveData<List<Category>> searchCategories(String searchQuery) {
        return categoryDao.searchCategories(searchQuery);
    }

    public LiveData<List<Category>> searchCategoriesByStore(String searchQuery, int storeId) {
        return categoryDao.searchCategoriesByStore(searchQuery, storeId);
    }

    public LiveData<Integer> getProductCountByCategory(String categoryName, int storeId) {
        return categoryDao.getProductCountByCategory(categoryName, storeId);
    }

    public interface OnCompleteListener {
        void onComplete(boolean success);
    }
} 