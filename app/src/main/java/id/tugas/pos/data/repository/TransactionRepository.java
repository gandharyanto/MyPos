package id.tugas.pos.data.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import id.tugas.pos.data.database.PosDatabase;
import id.tugas.pos.data.database.TransactionDao;
import id.tugas.pos.data.database.TransactionItemDao;
import id.tugas.pos.data.model.Transaction;
import id.tugas.pos.data.model.TransactionItem;

public class TransactionRepository {
    
    private TransactionDao transactionDao;
    private TransactionItemDao transactionItemDao;
    private LiveData<List<Transaction>> allTransactions;
    private LiveData<Integer> completedTransactionCount;
    private LiveData<Double> totalRevenue;
    private LiveData<Integer> pendingTransactionCount;
    
    public TransactionRepository(Application application) {
        PosDatabase database = PosDatabase.getInstance(application);
        transactionDao = database.transactionDao();
        transactionItemDao = database.transactionItemDao();
        allTransactions = transactionDao.getAllTransactions();
        completedTransactionCount = transactionDao.getCompletedTransactionCount();
        totalRevenue = transactionDao.getTotalRevenue();
        pendingTransactionCount = transactionDao.getPendingTransactionCount();
    }
    
    // Transaction operations
    public void insert(Transaction transaction) {
        new InsertTransactionAsyncTask(transactionDao).execute(transaction);
    }
    
    public void update(Transaction transaction) {
        new UpdateTransactionAsyncTask(transactionDao).execute(transaction);
    }
    
    public void delete(Transaction transaction) {
        new DeleteTransactionAsyncTask(transactionDao).execute(transaction);
    }
    
    public LiveData<Transaction> getTransactionById(int id) {
        return transactionDao.getTransactionById(id);
    }
    
    public LiveData<Transaction> getTransactionByNumber(String transactionNumber) {
        return transactionDao.getTransactionByNumber(transactionNumber);
    }
    
    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }
    
    public LiveData<List<Transaction>> getTransactionsByStatus(String status) {
        return transactionDao.getTransactionsByStatus(status);
    }
    
    public LiveData<List<Transaction>> getTransactionsByUser(int userId) {
        return transactionDao.getTransactionsByUser(userId);
    }
    
    public LiveData<List<Transaction>> getTransactionsByDateRange(long startDate, long endDate) {
        return transactionDao.getTransactionsByDateRange(startDate, endDate);
    }
    
    public LiveData<List<Transaction>> getTransactionsByPaymentMethod(String paymentMethod) {
        return transactionDao.getTransactionsByPaymentMethod(paymentMethod);
    }
    
    public LiveData<Integer> getCompletedTransactionCount() {
        return completedTransactionCount;
    }
    
    public LiveData<Double> getTotalRevenue() {
        return totalRevenue;
    }
    
    public LiveData<Double> getRevenueByDateRange(long startDate, long endDate) {
        return transactionDao.getRevenueByDateRange(startDate, endDate);
    }
    
    public LiveData<Double> getRevenueByUser(int userId) {
        return transactionDao.getRevenueByUser(userId);
    }
    
    public LiveData<List<Transaction>> getPendingTransactions() {
        return transactionDao.getPendingTransactions();
    }
    
    public LiveData<Integer> getPendingTransactionCount() {
        return pendingTransactionCount;
    }
    
    public void updateTransactionStatus(int transactionId, String status) {
        new UpdateTransactionStatusAsyncTask(transactionDao).execute(transactionId, status);
    }
    
    public LiveData<List<Transaction>> getRecentTransactions(int limit) {
        return transactionDao.getRecentTransactions(limit);
    }
    
    // TransactionItem operations
    public void insertTransactionItem(TransactionItem transactionItem) {
        new InsertTransactionItemAsyncTask(transactionItemDao).execute(transactionItem);
    }
    
    public void updateTransactionItem(TransactionItem transactionItem) {
        new UpdateTransactionItemAsyncTask(transactionItemDao).execute(transactionItem);
    }
    
    public void deleteTransactionItem(TransactionItem transactionItem) {
        new DeleteTransactionItemAsyncTask(transactionItemDao).execute(transactionItem);
    }
    
    public LiveData<List<TransactionItem>> getTransactionItemsByTransactionId(int transactionId) {
        return transactionItemDao.getTransactionItemsByTransactionId(transactionId);
    }
    
    public LiveData<List<TransactionItem>> getTransactionItemsByProductId(int productId) {
        return transactionItemDao.getTransactionItemsByProductId(productId);
    }
    
    public LiveData<Integer> getTransactionItemCount(int transactionId) {
        return transactionItemDao.getTransactionItemCount(transactionId);
    }
    
    public LiveData<Double> getTransactionTotal(int transactionId) {
        return transactionItemDao.getTransactionTotal(transactionId);
    }
    
    public LiveData<Integer> getTotalQuantitySold(int productId) {
        return transactionItemDao.getTotalQuantitySold(productId);
    }
    
    public LiveData<Double> getTotalRevenueByProduct(int productId) {
        return transactionItemDao.getTotalRevenueByProduct(productId);
    }
    
    public List<TransactionItem> getTransactionItemsByTransactionIdSync(int transactionId) {
        return transactionItemDao.getTransactionItemsByTransactionIdSync(transactionId);
    }
    
    public void deleteTransactionItemsByTransactionId(int transactionId) {
        new DeleteTransactionItemsByTransactionIdAsyncTask(transactionItemDao).execute(transactionId);
    }
    
    // AsyncTask classes for Transaction
    private static class InsertTransactionAsyncTask extends AsyncTask<Transaction, Void, Void> {
        private TransactionDao transactionDao;
        
        InsertTransactionAsyncTask(TransactionDao transactionDao) {
            this.transactionDao = transactionDao;
        }
        
        @Override
        protected Void doInBackground(Transaction... transactions) {
            transactionDao.insert(transactions[0]);
            return null;
        }
    }
    
    private static class UpdateTransactionAsyncTask extends AsyncTask<Transaction, Void, Void> {
        private TransactionDao transactionDao;
        
        UpdateTransactionAsyncTask(TransactionDao transactionDao) {
            this.transactionDao = transactionDao;
        }
        
        @Override
        protected Void doInBackground(Transaction... transactions) {
            transactionDao.update(transactions[0]);
            return null;
        }
    }
    
    private static class DeleteTransactionAsyncTask extends AsyncTask<Transaction, Void, Void> {
        private TransactionDao transactionDao;
        
        DeleteTransactionAsyncTask(TransactionDao transactionDao) {
            this.transactionDao = transactionDao;
        }
        
        @Override
        protected Void doInBackground(Transaction... transactions) {
            transactionDao.delete(transactions[0]);
            return null;
        }
    }
    
    private static class UpdateTransactionStatusAsyncTask extends AsyncTask<Object, Void, Void> {
        private TransactionDao transactionDao;
        
        UpdateTransactionStatusAsyncTask(TransactionDao transactionDao) {
            this.transactionDao = transactionDao;
        }
        
        @Override
        protected Void doInBackground(Object... params) {
            int transactionId = (Integer) params[0];
            String status = (String) params[1];
            transactionDao.updateTransactionStatus(transactionId, status);
            return null;
        }
    }
    
    // AsyncTask classes for TransactionItem
    private static class InsertTransactionItemAsyncTask extends AsyncTask<TransactionItem, Void, Void> {
        private TransactionItemDao transactionItemDao;
        
        InsertTransactionItemAsyncTask(TransactionItemDao transactionItemDao) {
            this.transactionItemDao = transactionItemDao;
        }
        
        @Override
        protected Void doInBackground(TransactionItem... transactionItems) {
            transactionItemDao.insert(transactionItems[0]);
            return null;
        }
    }
    
    private static class UpdateTransactionItemAsyncTask extends AsyncTask<TransactionItem, Void, Void> {
        private TransactionItemDao transactionItemDao;
        
        UpdateTransactionItemAsyncTask(TransactionItemDao transactionItemDao) {
            this.transactionItemDao = transactionItemDao;
        }
        
        @Override
        protected Void doInBackground(TransactionItem... transactionItems) {
            transactionItemDao.update(transactionItems[0]);
            return null;
        }
    }
    
    private static class DeleteTransactionItemAsyncTask extends AsyncTask<TransactionItem, Void, Void> {
        private TransactionItemDao transactionItemDao;
        
        DeleteTransactionItemAsyncTask(TransactionItemDao transactionItemDao) {
            this.transactionItemDao = transactionItemDao;
        }
        
        @Override
        protected Void doInBackground(TransactionItem... transactionItems) {
            transactionItemDao.delete(transactionItems[0]);
            return null;
        }
    }
    
    private static class DeleteTransactionItemsByTransactionIdAsyncTask extends AsyncTask<Integer, Void, Void> {
        private TransactionItemDao transactionItemDao;
        
        DeleteTransactionItemsByTransactionIdAsyncTask(TransactionItemDao transactionItemDao) {
            this.transactionItemDao = transactionItemDao;
        }
        
        @Override
        protected Void doInBackground(Integer... transactionIds) {
            transactionItemDao.deleteTransactionItemsByTransactionId(transactionIds[0]);
            return null;
        }
    }
} 