package id.tugas.pos.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import id.tugas.pos.data.model.ModalAwal;

@Dao
public interface ModalAwalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ModalAwal modalAwal);

    @Update
    void update(ModalAwal modalAwal);

    @Query("SELECT * FROM modal_awal WHERE tanggal = :tanggal AND storeId = :storeId LIMIT 1")
    ModalAwal getModalAwalByTanggal(long tanggal, int storeId);

    @Query("SELECT * FROM modal_awal WHERE storeId = :storeId ORDER BY tanggal DESC LIMIT 1")
    ModalAwal getLastModalAwal(int storeId);

    @Query("SELECT * FROM modal_awal WHERE storeId = :storeId ORDER BY tanggal DESC")
    List<ModalAwal> getAllModalAwalByStore(int storeId);

    @Query("SELECT * FROM modal_awal WHERE storeId = :storeId ORDER BY tanggal DESC")
    LiveData<List<ModalAwal>> getAllModalAwalByStoreLive(int storeId);

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM modal_awal WHERE storeId = :storeId")
    double getTotalModalByStore(int storeId);

    @Query("SELECT COALESCE(MAX(saldoSesudah), 0) FROM modal_awal WHERE storeId = :storeId")
    double getSaldoModalSaatIni(int storeId);

    @Query("SELECT * FROM modal_awal WHERE storeId = :storeId AND tanggal = :today LIMIT 1")
    ModalAwal getModalAwalToday(int storeId, long today);

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM modal_awal WHERE storeId = :storeId AND tanggal = :today AND tipe = 'ADD_CAPITAL'")
    double getTotalPenambahanHariIni(int storeId, long today);
}
