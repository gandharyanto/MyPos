package id.tugas.pos.data.repository;

import android.app.Application;
import id.tugas.pos.data.database.PosDatabase;
import id.tugas.pos.data.database.ModalAwalDao;
import id.tugas.pos.data.model.ModalAwal;

public class ModalAwalRepository {
    private final ModalAwalDao modalAwalDao;

    public ModalAwalRepository(Application application) {
        PosDatabase db = PosDatabase.getInstance(application);
        modalAwalDao = db.modalAwalDao();
    }

    public void insert(ModalAwal modalAwal) {
        new Thread(() -> modalAwalDao.insert(modalAwal)).start();
    }

    public ModalAwal getModalAwalByTanggal(long tanggal, int storeId) {
        return modalAwalDao.getModalAwalByTanggal(tanggal, storeId);
    }

    public ModalAwal getLastModalAwal(int storeId) {
        return modalAwalDao.getLastModalAwal(storeId);
    }
} 