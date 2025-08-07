package id.tugas.pos.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import id.tugas.pos.data.model.Expense;
import id.tugas.pos.data.model.Product;
import id.tugas.pos.data.model.Store;
import id.tugas.pos.data.model.Transaction;
import id.tugas.pos.data.model.TransactionItem;
import id.tugas.pos.data.model.User;

import id.tugas.pos.data.model.StockIn;
import id.tugas.pos.data.database.StockInDao;
import id.tugas.pos.data.model.ModalAwal;
import id.tugas.pos.data.database.ModalAwalDao;
import id.tugas.pos.data.model.Category;
import id.tugas.pos.data.database.CategoryDao;

@Database(entities = {
        User.class,
        Product.class,
        Transaction.class,
        TransactionItem.class,
        Expense.class,
        Store.class,
        StockIn.class,
        ModalAwal.class,
        Category.class
}, version = 11, exportSchema = false)
public abstract class PosDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "pos_database";
    private static PosDatabase instance;
    
    public abstract UserDao userDao();
    public abstract ProductDao productDao();
    public abstract TransactionDao transactionDao();
    public abstract TransactionItemDao transactionItemDao();
    public abstract ExpenseDao expenseDao();
    public abstract StoreDao storeDao();

    public abstract StockInDao stockInDao();
    public abstract ModalAwalDao modalAwalDao();
    public abstract CategoryDao categoryDao();
    
    public static synchronized PosDatabase getInstance(Context context) {
        if (instance == null) {
            android.util.Log.d("PosDatabase", "Creating new database instance");
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    PosDatabase.class,
                    DATABASE_NAME)
                    .addMigrations(MIGRATION_10_11)
                    .fallbackToDestructiveMigration()
                    .build();
            android.util.Log.d("PosDatabase", "Database instance created successfully");
        } else {
            android.util.Log.d("PosDatabase", "Using existing database instance");
        }
        return instance;
    }


    
    // Migrasi dari versi 10 ke 11: hapus tabel saving
    public static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Hapus tabel saving
            try {
                database.execSQL("DROP TABLE IF EXISTS saving");
            } catch (Exception e) {
                // Tabel tidak ada, skip
            }
        }
    };
} 