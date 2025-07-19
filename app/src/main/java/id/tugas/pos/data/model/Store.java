package id.tugas.pos.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stores")
public class Store {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String address;
    private String phone;
    private long createdAt;

    public Store() {
        this.createdAt = System.currentTimeMillis();
    }

    public Store(String name, String address, String phone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return name != null ? name : "Toko " + id;
    }
} 