package id.tugas.pos.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;

@Entity(
    tableName = "users",
    indices = {
        @Index(value = {"username"}, unique = true),
        @Index(value = {"email"}, unique = true)
    }
)
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String role; // "ADMIN" or "USER"
    private boolean isActive;
    private long createdAt;
    private long lastLogin;
    private Integer storeId; // null untuk admin, id toko untuk user

    public User() {
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
    }

    public User(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
    }

    public User(String username, String email, String password, String fullName, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isUser() {
        return "USER".equals(role);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
} 