package id.tugas.pos.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
import id.tugas.pos.data.model.StockIn;
import id.tugas.pos.ui.report.LaporanStokItem;

@Dao
public interface StockInDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StockIn stockIn);

    @Query("SELECT productName as namaProduk, SUM(quantity) as stokMasuk, 0 as stokKeluar, 0 as stokTersisa FROM stock_in WHERE createdAt BETWEEN :startDate AND :endDate GROUP BY productId")
    List<LaporanStokItem> getLaporanStokMasuk(long startDate, long endDate);
} 