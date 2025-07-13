package id.tugas.pos.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import id.tugas.pos.data.model.User;

@Dao
public interface UserDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);
    
    @Update
    void update(User user);
    
    @Delete
    void delete(User user);
    
    @Query("SELECT * FROM users WHERE id = :id")
    LiveData<User> getUserById(int id);
    
    @Query("SELECT * FROM users WHERE username = :username AND password = :password AND isActive = 1")
    LiveData<User> login(String username, String password);
    
    @Query("SELECT * FROM users WHERE username = :username")
    LiveData<User> getUserByUsername(String username);
    
    @Query("SELECT * FROM users WHERE isActive = 1 ORDER BY fullName ASC")
    LiveData<List<User>> getAllActiveUsers();
    
    @Query("SELECT * FROM users WHERE role = :role AND isActive = 1 ORDER BY fullName ASC")
    LiveData<List<User>> getUsersByRole(String role);
    
    @Query("SELECT COUNT(*) FROM users WHERE isActive = 1")
    LiveData<Integer> getActiveUserCount();
    
    @Query("UPDATE users SET lastLogin = :timestamp WHERE id = :userId")
    void updateLastLogin(int userId, long timestamp);
    
    @Query("SELECT * FROM users WHERE isActive = 1")
    List<User> getAllActiveUsersSync();
} 