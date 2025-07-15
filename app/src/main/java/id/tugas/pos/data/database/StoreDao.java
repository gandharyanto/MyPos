package id.tugas.pos.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import id.tugas.pos.data.model.Store;

@Dao
public interface StoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Store store);

    @Update
    void update(Store store);

    @Delete
    void delete(Store store);

    @Query("SELECT * FROM stores WHERE id = :id")
    LiveData<Store> getStoreById(int id);

    @Query("SELECT * FROM stores ORDER BY name ASC")
    LiveData<List<Store>> getAllStores();
} 