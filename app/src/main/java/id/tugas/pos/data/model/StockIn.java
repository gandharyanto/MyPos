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
    public int storeId; // Tambahkan storeId
    public String type; // "IN" atau "OUT"
    // Bisa ditambah: supplier, keterangan, userId, dsb
}
