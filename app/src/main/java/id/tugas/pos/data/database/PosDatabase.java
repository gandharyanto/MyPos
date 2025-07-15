package id.tugas.pos.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import id.tugas.pos.data.model.Expense;
import id.tugas.pos.data.model.Product;
import id.tugas.pos.data.model.Store;
import id.tugas.pos.data.model.Transaction;
import id.tugas.pos.data.model.TransactionItem;
import id.tugas.pos.data.model.User;

@Database(entities = {
        User.class,
        Product.class,
        Transaction.class,
        TransactionItem.class,
        Expense.class,
        Store.class
}, version = 2, exportSchema = false)
public abstract class PosDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "pos_database";
    private static PosDatabase instance;
    
    public abstract UserDao userDao();
    public abstract ProductDao productDao();
    public abstract TransactionDao transactionDao();
    public abstract TransactionItemDao transactionItemDao();
    public abstract ExpenseDao expenseDao();
    public abstract StoreDao storeDao();
    
    public static synchronized PosDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    PosDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
} 