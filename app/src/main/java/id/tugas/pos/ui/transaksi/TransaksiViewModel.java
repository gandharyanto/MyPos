package id.tugas.pos.ui.transaksi;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import id.tugas.pos.data.model.Product;
import id.tugas.pos.data.model.Transaction;
import id.tugas.pos.data.model.TransactionItem;
import id.tugas.pos.data.repository.ProductRepository;
import id.tugas.pos.data.repository.TransactionRepository;
import id.tugas.pos.utils.PrinterUtils;
import id.tugas.pos.utils.CurrencyUtils;

public class TransaksiViewModel extends AndroidViewModel {

    private ProductRepository productRepository;
    private TransactionRepository transactionRepository;
    private LiveData<List<Product>> allProducts;
    private MutableLiveData<List<TransactionItem>> cartItems = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public TransaksiViewModel(@NonNull Application application) {
        super(application);
        productRepository = new ProductRepository(application);
        transactionRepository = new TransactionRepository(application);
        allProducts = productRepository.getAllProducts();
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public LiveData<List<TransactionItem>> getCartItems() {
        return cartItems;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void addToCart(Product product) {
        List<TransactionItem> currentCart = cartItems.getValue();
        if (currentCart == null) {
            currentCart = new ArrayList<>();
        }

        // Check if product already exists in cart
        boolean found = false;
        for (TransactionItem item : currentCart) {
            if (item.getProductId() == product.getId()) {
                // Increase quantity if stock allows
                if (item.getQuantity() < product.getStock()) {
                    item.setQuantity(item.getQuantity() + 1);
                    item.setSubtotal(item.getQuantity() * item.getPrice());
                }
                found = true;
                break;
            }
        }

        if (!found) {
            // Add new item to cart
            TransactionItem newItem = new TransactionItem(
                product.getId(),
                product.getName(),
                product.getCode(),
                product.getPrice(),
                1,
                product.getPrice()
            );
            currentCart.add(newItem);
        }

        cartItems.setValue(currentCart);
    }

    public void removeFromCart(TransactionItem item) {
        List<TransactionItem> currentCart = cartItems.getValue();
        if (currentCart != null) {
            currentCart.remove(item);
            cartItems.setValue(currentCart);
        }
    }

    public void updateCartItemQuantity(TransactionItem item, int newQuantity) {
        List<TransactionItem> currentCart = cartItems.getValue();
        if (currentCart != null) {
            for (TransactionItem cartItem : currentCart) {
                if (cartItem.getProductId() == item.getProductId()) {
                    cartItem.setQuantity(newQuantity);
                    cartItem.setSubtotal(newQuantity * cartItem.getPrice());
                    break;
                }
            }
            cartItems.setValue(currentCart);
        }
    }

    public void clearCart() {
        cartItems.setValue(new ArrayList<>());
    }

    public void processTransaction(List<TransactionItem> items, double totalAmount) {
        isLoading.setValue(true);
        
        // Create transaction
        Transaction transaction = new Transaction(totalAmount, "CASH");
        
        transactionRepository.addTransaction(transaction, new TransactionRepository.OnTransactionOperationListener() {
            @Override
            public void onSuccess() {
                // Add transaction items
                for (TransactionItem item : items) {
                    item.setTransactionId(transaction.getId());
                    transactionRepository.addTransactionItem(item, new TransactionRepository.OnTransactionOperationListener() {
                        @Override
                        public void onSuccess() {
                            // Update product stock
                            updateProductStock(item);
                        }

                        @Override
                        public void onError(String error) {
                            mainHandler.post(() -> {
                                errorMessage.setValue("Gagal menyimpan item transaksi: " + error);
                            });
                        }
                    });
                }
                
                // Print receipt
                printReceipt(transaction, items);
                
                // Clear cart
                mainHandler.post(() -> {
                    clearCart();
                    isLoading.setValue(false);
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    errorMessage.setValue("Gagal memproses transaksi: " + error);
                    isLoading.setValue(false);
                });
            }
        });
    }

    private void updateProductStock(TransactionItem item) {
        productRepository.getProductById(item.getProductId(), new ProductRepository.OnProductSearchListener() {
            @Override
            public void onSuccess(List<Product> products) {
                if (!products.isEmpty()) {
                    Product product = products.get(0);
                    int newStock = product.getStock() - item.getQuantity();
                    if (newStock >= 0) {
                        product.setStock(newStock);
                        productRepository.updateProduct(product, new ProductRepository.OnProductOperationListener() {
                            @Override
                            public void onSuccess() {
                                // Stock updated successfully
                            }

                            @Override
                            public void onError(String error) {
                                mainHandler.post(() -> {
                                    errorMessage.setValue("Gagal update stok: " + error);
                                });
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    errorMessage.setValue("Gagal mendapatkan data produk: " + error);
                });
            }
        });
    }

    private void printReceipt(Transaction transaction, List<TransactionItem> items) {
        // Print using PrinterUtils
        PrinterUtils.printReceipt(getApplication(), transaction, items, null);
    }

    public LiveData<List<Product>> getAllProductsByStore(int storeId) {
        return productRepository.getAllProductsByStore(storeId);
    }
} 