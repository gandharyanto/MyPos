package id.tugas.pos.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;
import id.tugas.pos.data.model.Saving;

@Dao
public interface SavingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Saving saving);

    @Update
    void update(Saving saving);

    @Delete
    void delete(Saving saving);

    @Query("SELECT * FROM saving WHERE id = :id")
    Saving getSavingById(int id);

    @Query("SELECT * FROM saving ORDER BY savingDate DESC")
    List<Saving> getAllSaving();

    @Query("SELECT savingDate as tanggal, amount as nominal, description as keterangan " +
           "FROM saving " +
           "WHERE savingDate BETWEEN :startDate AND :endDate " +
           "ORDER BY savingDate ASC")
    List<id.tugas.pos.ui.report.LaporanSavingItem> getLaporanSaving(long startDate, long endDate);
} 