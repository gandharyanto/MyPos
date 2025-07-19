package id.tugas.pos.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
import id.tugas.pos.data.model.ModalAwal;

@Dao
public interface ModalAwalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ModalAwal modalAwal);

    @Query("SELECT * FROM modal_awal WHERE tanggal = :tanggal AND storeId = :storeId LIMIT 1")
    ModalAwal getModalAwalByTanggal(long tanggal, int storeId);

    @Query("SELECT * FROM modal_awal WHERE storeId = :storeId ORDER BY tanggal DESC LIMIT 1")
    ModalAwal getLastModalAwal(int storeId);

    @Query("SELECT * FROM modal_awal WHERE storeId = :storeId ORDER BY tanggal DESC")
    List<ModalAwal> getAllModalAwalByStore(int storeId);
} 