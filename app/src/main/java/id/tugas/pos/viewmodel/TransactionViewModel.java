package id.tugas.pos.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import id.tugas.pos.data.model.Product;
import id.tugas.pos.data.model.Transaction;
import id.tugas.pos.data.model.TransactionItem;
import id.tugas.pos.data.repository.ProductRepository;
import id.tugas.pos.data.repository.TransactionRepository;

public class TransactionViewModel extends AndroidViewModel {

    private TransactionRepository transactionRepository;
    private ProductRepository productRepository;
    private MutableLiveData<List<TransactionItem>> cartItems;
    private MutableLiveData<Double> totalAmount;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;
    private ExecutorService executorService;

    public TransactionViewModel(Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        productRepository = new ProductRepository(application);
        cartItems = new MutableLiveData<>(new ArrayList<>());
        totalAmount = new MutableLiveData<>(0.0);
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<TransactionItem>> getCartItems() {
        return cartItems;
    }

    public LiveData<Double> getTotalAmount() {
        return totalAmount;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void addToCart(Product product, int quantity) {
        List<TransactionItem> currentCart = cartItems.getValue();
        if (currentCart == null) {
            currentCart = new ArrayList<>();
        }

        // Check if product already in cart
        boolean found = false;
        for (TransactionItem item : currentCart) {
            if (item.getProductId() == product.getId()) {
                int newQuantity = item.getQuantity() + quantity;
                if (newQuantity <= product.getStock()) {
                    item.setQuantity(newQuantity);
                    item.setSubtotal(product.getPrice() * newQuantity);
                } else {
                    errorMessage.setValue("Stok tidak mencukupi");
                    return;
                }
                found = true;
                break;
            }
        }

        if (!found) {
            if (quantity <= product.getStock()) {
                TransactionItem newItem = new TransactionItem();
                newItem.setProductId(product.getId());
                newItem.setProductName(product.getName());
                newItem.setPrice(product.getPrice());
                newItem.setQuantity(quantity);
                newItem.setSubtotal(product.getPrice() * quantity);
                currentCart.add(newItem);
            } else {
                errorMessage.setValue("Stok tidak mencukupi");
                return;
            }
        }

        cartItems.setValue(currentCart);
        calculateTotal();
    }

    public void updateCartItemQuantity(int position, int quantity) {
        List<TransactionItem> currentCart = cartItems.getValue();
        if (currentCart != null && position < currentCart.size()) {
            TransactionItem item = currentCart.get(position);

            // Check stock availability
            executorService.execute(() -> {
                LiveData<Product> productLiveData = productRepository.getProductById(item.getProductId());
                Product product = productLiveData.getValue();
                if (product != null && quantity <= product.getStock()) {
                    item.setQuantity(quantity);
                    item.setSubtotal(item.getPrice() * quantity);
                    cartItems.postValue(currentCart);
                    calculateTotal();
                } else {
                    errorMessage.postValue("Stok tidak mencukupi");
                }
            });
        }
    }

    public void removeFromCart(int position) {
        List<TransactionItem> currentCart = cartItems.getValue();
        if (currentCart != null && position < currentCart.size()) {
            currentCart.remove(position);
            cartItems.setValue(currentCart);
            calculateTotal();
        }
    }

    public void clearCart() {
        cartItems.setValue(new ArrayList<>());
        totalAmount.setValue(0.0);
    }

    private void calculateTotal() {
        List<TransactionItem> currentCart = cartItems.getValue();
        if (currentCart != null) {
            double total = 0.0;
            for (TransactionItem item : currentCart) {
                total += item.getSubtotal();
            }
            totalAmount.setValue(total);
        }
    }

    public void processTransaction(String paymentMethod, double amountPaid) {
        List<TransactionItem> currentCart = cartItems.getValue();
        if (currentCart == null || currentCart.isEmpty()) {
            errorMessage.setValue("Keranjang kosong");
            return;
        }

        double total = totalAmount.getValue() != null ? totalAmount.getValue() : 0.0;
        if (amountPaid < total) {
            errorMessage.setValue("Pembayaran kurang");
            return;
        }

        isLoading.setValue(true);
        executorService.execute(() -> {
            try {
                // Create transaction
                Transaction transaction = new Transaction();
                transaction.setTotalAmount(total);
                transaction.setPaymentMethod(paymentMethod);
                transaction.setAmountPaid(amountPaid);
                transaction.setChange(amountPaid - total);
                transaction.setStatus("completed");
                transaction.setCreatedAt(System.currentTimeMillis());

                long transactionId = transactionRepository.insertTransaction(transaction);

                // Add transaction items
                for (TransactionItem item : currentCart) {
                    item.setTransactionId((int) transactionId);
                    item.setCreatedAt(System.currentTimeMillis());
                    transactionRepository.insertTransactionItem(item);

                    // Update product stock
                    LiveData<Product> productLiveData = productRepository.getProductById(item.getProductId());
                    Product product = productLiveData.getValue();
                    if (product != null) {
                        product.setStock(product.getStock() - item.getQuantity());
                        productRepository.update(product);
                    }
                }

                // Clear cart
                clearCart();
                isLoading.postValue(false);

            } catch (Exception e) {
                errorMessage.postValue("Gagal memproses transaksi: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public LiveData<List<Transaction>> getRecentTransactions() {
        return transactionRepository.getRecentTransactions();
    }

    public LiveData<List<Transaction>> getTransactionsByStore(int storeId) {
        return transactionRepository.getAllTransactionsByStore(storeId);
    }

    public void updateTransaction(Transaction transaction, TransactionRepository.OnTransactionOperationListener listener) {
        isLoading.setValue(true);
        executorService.execute(() -> {
            try {
                transactionRepository.updateTransaction(transaction, listener);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Gagal mengupdate transaksi: " + e.getMessage());
                isLoading.postValue(false);
                if (listener != null) {
                    listener.onError(e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
} 