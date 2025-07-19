package id.tugas.pos.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stock_in")
public class StockIn {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int productId;
    public String productName;
    public int quantity;
    public long createdAt;
    // Bisa ditambah: supplier, keterangan, userId, dsb
} 