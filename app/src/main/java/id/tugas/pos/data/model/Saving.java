package id.tugas.pos.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "saving")
public class Saving {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long savingDate;
    public double amount;
    public String description;
    public int storeId;

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getSavingDate() { return savingDate; }
    public void setSavingDate(long savingDate) { this.savingDate = savingDate; }
    public int getStoreId() { return storeId; }
    public void setStoreId(int storeId) { this.storeId = storeId; }
} 