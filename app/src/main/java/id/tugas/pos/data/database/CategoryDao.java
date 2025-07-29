package id.tugas.pos.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import id.tugas.pos.data.model.Category;

@Dao
public interface CategoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Category category);
    
    @Update
    void update(Category category);
    
    @Delete
    void delete(Category category);
    
    @Query("SELECT * FROM categories WHERE id = :id")
    LiveData<Category> getCategoryById(int id);
    
    @Query("SELECT * FROM categories WHERE name = :name AND storeId = :storeId")
    LiveData<Category> getCategoryByName(String name, int storeId);
    
    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY name ASC")
    LiveData<List<Category>> getAllActiveCategories();
    
    @Query("SELECT * FROM categories WHERE isActive = 1 AND storeId = :storeId ORDER BY name ASC")
    LiveData<List<Category>> getAllActiveCategoriesByStore(int storeId);
    
    @Query("SELECT name FROM categories WHERE isActive = 1 AND storeId = :storeId ORDER BY name ASC")
    LiveData<List<String>> getAllCategoryNamesByStore(int storeId);
    
    @Query("SELECT COUNT(*) FROM categories WHERE isActive = 1 AND storeId = :storeId")
    LiveData<Integer> getCategoryCountByStore(int storeId);
    
    @Query("SELECT * FROM categories WHERE name LIKE '%' || :searchQuery || '%' AND isActive = 1 ORDER BY name ASC")
    LiveData<List<Category>> searchCategories(String searchQuery);
    
    @Query("SELECT * FROM categories WHERE name LIKE '%' || :searchQuery || '%' AND isActive = 1 AND storeId = :storeId ORDER BY name ASC")
    LiveData<List<Category>> searchCategoriesByStore(String searchQuery, int storeId);
    
    @Query("UPDATE categories SET isActive = 0 WHERE id = :categoryId")
    void softDeleteCategory(int categoryId);
    
    @Query("SELECT COUNT(*) FROM products WHERE category = :categoryName AND isActive = 1 AND storeId = :storeId")
    LiveData<Integer> getProductCountByCategory(String categoryName, int storeId);
} 