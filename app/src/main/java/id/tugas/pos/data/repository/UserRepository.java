package id.tugas.pos.data.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import id.tugas.pos.data.database.PosDatabase;
import id.tugas.pos.data.database.UserDao;
import id.tugas.pos.data.model.User;

public class UserRepository {
    
    private UserDao userDao;
    private LiveData<List<User>> allActiveUsers;
    private LiveData<Integer> activeUserCount;
    
    public UserRepository(Application application) {
        PosDatabase database = PosDatabase.getInstance(application);
        userDao = database.userDao();
        allActiveUsers = userDao.getAllActiveUsers();
        activeUserCount = userDao.getActiveUserCount();
    }
    
    // Insert user
    public void insert(User user) {
        new InsertUserAsyncTask(userDao).execute(user);
    }
    
    // Update user
    public void update(User user) {
        new UpdateUserAsyncTask(userDao).execute(user);
    }
    
    // Delete user
    public void delete(User user) {
        new DeleteUserAsyncTask(userDao).execute(user);
    }
    
    // Get user by ID
    public LiveData<User> getUserById(int id) {
        return userDao.getUserById(id);
    }
    
    // Login
    public LiveData<User> login(String username, String password) {
        return userDao.login(username, password);
    }

    // Login by email (untuk admin)
    public LiveData<User> loginByEmail(String email, String password) {
        return userDao.loginByEmail(email, password);
    }
    
    // Get user by username
    public LiveData<User> getUserByUsername(String username) {
        return userDao.getUserByUsername(username);
    }

    // Get user by email
    public LiveData<User> getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }
    
    // Get all active users
    public LiveData<List<User>> getAllActiveUsers() {
        return allActiveUsers;
    }
    
    // Get users by role
    public LiveData<List<User>> getUsersByRole(String role) {
        return userDao.getUsersByRole(role);
    }
    
    // Get active user count
    public LiveData<Integer> getActiveUserCount() {
        return activeUserCount;
    }
    
    // Update last login
    public void updateLastLogin(int userId, long timestamp) {
        new UpdateLastLoginAsyncTask(userDao).execute(userId, timestamp);
    }
    
    // Get all active users synchronously
    public List<User> getAllActiveUsersSync() {
        return userDao.getAllActiveUsersSync();
    }
    
    // AsyncTask classes
    private static class InsertUserAsyncTask extends AsyncTask<User, Void, Void> {
        private UserDao userDao;
        
        InsertUserAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }
        
        @Override
        protected Void doInBackground(User... users) {
            userDao.insert(users[0]);
            return null;
        }
    }
    
    private static class UpdateUserAsyncTask extends AsyncTask<User, Void, Void> {
        private UserDao userDao;
        
        UpdateUserAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }
        
        @Override
        protected Void doInBackground(User... users) {
            userDao.update(users[0]);
            return null;
        }
    }
    
    private static class DeleteUserAsyncTask extends AsyncTask<User, Void, Void> {
        private UserDao userDao;
        
        DeleteUserAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }
        
        @Override
        protected Void doInBackground(User... users) {
            userDao.delete(users[0]);
            return null;
        }
    }
    
    private static class UpdateLastLoginAsyncTask extends AsyncTask<Object, Void, Void> {
        private UserDao userDao;
        
        UpdateLastLoginAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }
        
        @Override
        protected Void doInBackground(Object... params) {
            int userId = (Integer) params[0];
            long timestamp = (Long) params[1];
            userDao.updateLastLogin(userId, timestamp);
            return null;
        }
    }
} 