package id.tugas.pos.data.repository;

import android.app.Application;
import id.tugas.pos.data.database.PosDatabase;
import id.tugas.pos.data.database.StockInDao;
import id.tugas.pos.data.database.ProductDao;
import id.tugas.pos.data.model.StockIn;

public class StockInRepository {
    private final StockInDao stockInDao;
    private final ProductDao productDao;

    public StockInRepository(Application application) {
        PosDatabase db = PosDatabase.getInstance(application);
        stockInDao = db.stockInDao();
        productDao = db.productDao();
    }

    public void insert(StockIn stockIn) {
        stockInDao.insert(stockIn);
        productDao.increaseStock(stockIn.productId, stockIn.quantity);
    }
} 