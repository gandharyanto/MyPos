package id.tugas.pos.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import id.tugas.pos.data.database.PosDatabase;
import id.tugas.pos.data.database.ProductDao;
import id.tugas.pos.data.database.TransactionItemDao;
import id.tugas.pos.data.database.StockInDao;
import id.tugas.pos.data.model.StockIn;
import id.tugas.pos.ui.report.LaporanStokItem;

public class StokRepository {
    private ProductDao productDao;
    private TransactionItemDao transactionItemDao;
    private StockInDao stockInDao;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public StokRepository(Application application) {
        PosDatabase database = PosDatabase.getInstance(application);
        productDao = database.productDao();
        transactionItemDao = database.transactionItemDao();
        stockInDao = database.stockInDao();
    }

    public LiveData<List<LaporanStokItem>> getLaporanStok(long startDate, long endDate) {
        MutableLiveData<List<LaporanStokItem>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<LaporanStokItem> stokTersisa = productDao.getLaporanStokTersisa();
            List<LaporanStokItem> stokKeluar = transactionItemDao.getLaporanStokKeluar(startDate, endDate);
            List<LaporanStokItem> stokMasuk = stockInDao.getLaporanStokMasuk(startDate, endDate);
            Map<String, LaporanStokItem> map = new HashMap<>();
            for (LaporanStokItem item : stokTersisa) {
                map.put(item.getNamaProduk(), new LaporanStokItem(item.getNamaProduk(), 0, 0, item.getStokTersisa()));
            }
            for (LaporanStokItem item : stokKeluar) {
                LaporanStokItem stok = map.get(item.getNamaProduk());
                if (stok != null) {
                    stok.setStokKeluar(item.getStokKeluar());
                } else {
                    map.put(item.getNamaProduk(), new LaporanStokItem(item.getNamaProduk(), 0, item.getStokKeluar(), 0));
                }
            }
            for (LaporanStokItem item : stokMasuk) {
                LaporanStokItem stok = map.get(item.getNamaProduk());
                if (stok != null) {
                    stok.setStokMasuk(item.getStokMasuk());
                } else {
                    map.put(item.getNamaProduk(), new LaporanStokItem(item.getNamaProduk(), item.getStokMasuk(), 0, 0));
                }
            }
            liveData.postValue(new java.util.ArrayList<>(map.values()));
        });
        return liveData;
    }
} 