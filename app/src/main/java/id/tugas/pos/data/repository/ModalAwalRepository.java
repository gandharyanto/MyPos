package id.tugas.pos.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import id.tugas.pos.data.database.PosDatabase;
import id.tugas.pos.data.database.ModalAwalDao;
import id.tugas.pos.data.model.ModalAwal;

public class ModalAwalRepository {
    private final ModalAwalDao modalAwalDao;
    private final ExecutorService executor;

    public ModalAwalRepository(Application application) {
        PosDatabase db = PosDatabase.getInstance(application);
        modalAwalDao = db.modalAwalDao();
        executor = Executors.newFixedThreadPool(2);
    }

    // Insert modal awal baru
    public void insert(ModalAwal modalAwal, Callback callback) {
        executor.execute(() -> {
            try {
                long id = modalAwalDao.insert(modalAwal);
                callback.onSuccess(id);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    // Update modal awal
    public void update(ModalAwal modalAwal, Callback callback) {
        executor.execute(() -> {
            try {
                modalAwalDao.update(modalAwal);
                callback.onSuccess(0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    // Tambah modal awal dengan perhitungan saldo
    public void tambahModalAwal(int storeId, double nominal, String keterangan, Callback callback) {
        executor.execute(() -> {
            try {
                long today = Long.parseLong(new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()));

                // Dapatkan saldo sebelumnya
                double saldoSebelum = getSaldoModalSaatIni(storeId);
                double saldoSesudah = saldoSebelum + nominal;

                ModalAwal modalAwal = new ModalAwal();
                modalAwal.setTanggal(today);
                modalAwal.setStoreId(storeId);
                modalAwal.setNominal(nominal);
                modalAwal.setSaldoSebelum(saldoSebelum);
                modalAwal.setSaldoSesudah(saldoSesudah);
                modalAwal.setTipe(saldoSebelum == 0 ? "INITIAL" : "ADD_CAPITAL");
                modalAwal.setKeterangan(keterangan != null ? keterangan :
                    (saldoSebelum == 0 ? "Modal awal" : "Tambah modal"));

                long id = modalAwalDao.insert(modalAwal);
                callback.onSuccess(id);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public ModalAwal getModalAwalByTanggal(long tanggal, int storeId) {
        return modalAwalDao.getModalAwalByTanggal(tanggal, storeId);
    }

    public ModalAwal getLastModalAwal(int storeId) {
        return modalAwalDao.getLastModalAwal(storeId);
    }

    public LiveData<List<ModalAwal>> getAllModalAwalByStoreLive(int storeId) {
        return modalAwalDao.getAllModalAwalByStoreLive(storeId);
    }

    public List<ModalAwal> getAllModalAwalByStore(int storeId) {
        return modalAwalDao.getAllModalAwalByStore(storeId);
    }

    public double getTotalModalByStore(int storeId) {
        return modalAwalDao.getTotalModalByStore(storeId);
    }

    public double getSaldoModalSaatIni(int storeId) {
        return modalAwalDao.getSaldoModalSaatIni(storeId);
    }

    public ModalAwal getModalAwalToday(int storeId, long today) {
        return modalAwalDao.getModalAwalToday(storeId, today);
    }

    public double getTotalPenambahanHariIni(int storeId, long today) {
        return modalAwalDao.getTotalPenambahanHariIni(storeId, today);
    }

    // Cek apakah modal perlu ditambah (ketika saldo hampir habis)
    public void cekModalHampirsHabis(int storeId, double batasMinimum, ModalHampirHabisCallback callback) {
        executor.execute(() -> {
            double saldoSaatIni = getSaldoModalSaatIni(storeId);
            if (saldoSaatIni <= batasMinimum) {
                callback.onModalHampirHabis(saldoSaatIni, batasMinimum);
            } else {
                callback.onModalCukup(saldoSaatIni);
            }
        });
    }

    public interface Callback {
        void onSuccess(long id);
        void onError(String error);
    }

    public interface ModalHampirHabisCallback {
        void onModalHampirHabis(double saldoSaatIni, double batasMinimum);
        void onModalCukup(double saldoSaatIni);
    }
}
