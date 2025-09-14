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
}, version = 14, exportSchema = false)
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
                    .addMigrations(MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)
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

    // Migrasi dari versi 11 ke 12: menambahkan kolom 'type' pada tabel stock_in
    public static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE stock_in ADD COLUMN type TEXT DEFAULT 'IN'");
        }
    };

    // Migration from version 12 to 13: add storeId column to transaction_items
    public static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE transaction_items ADD COLUMN storeId INTEGER NOT NULL DEFAULT 0");
        }
    };

    // Migration from version 13 to 14: add new fields to modal_awal table
    public static final Migration MIGRATION_13_14 = new Migration(13, 14) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add new columns to modal_awal table
            database.execSQL("ALTER TABLE modal_awal ADD COLUMN saldoSebelum REAL NOT NULL DEFAULT 0.0");
            database.execSQL("ALTER TABLE modal_awal ADD COLUMN saldoSesudah REAL NOT NULL DEFAULT 0.0");
            database.execSQL("ALTER TABLE modal_awal ADD COLUMN tipe TEXT DEFAULT 'INITIAL'");
            database.execSQL("ALTER TABLE modal_awal ADD COLUMN keterangan TEXT DEFAULT ''");
            database.execSQL("ALTER TABLE modal_awal ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0");
        }
    };
}
