package id.tugas.pos.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "modal_awal")
public class ModalAwal {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long tanggal; // format YYYYMMDD
    public int storeId;
    public double nominal;
} 