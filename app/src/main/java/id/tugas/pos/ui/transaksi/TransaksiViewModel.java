package id.tugas.pos.ui.transaksi;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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

    private static final String TAG = "TransaksiViewModel";
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
        Log.d(TAG, "addToCart: Adding product: " + product.getName() + " (ID: " + product.getId() + ")");
        List<TransactionItem> currentCart = cartItems.getValue();
        if (currentCart == null) {
            currentCart = new ArrayList<>();
            Log.d(TAG, "addToCart: Cart was null, created new ArrayList");
        }

        Log.d(TAG, "addToCart: Current cart size: " + currentCart.size());
        
        // Debug: Log semua item yang ada di cart
        for (int i = 0; i < currentCart.size(); i++) {
            TransactionItem item = currentCart.get(i);
            Log.d(TAG, "addToCart: Cart item " + i + ": ID=" + item.getProductId() + ", Name=" + item.getName() + ", Qty=" + item.getQuantity());
        }

        // Check if product already exists in cart
        boolean found = false;
        for (TransactionItem item : currentCart) {
            Log.d(TAG, "addToCart: Checking item ID: " + item.getProductId() + " vs product ID: " + product.getId());
            if (item.getProductId() == product.getId()) {
                Log.d(TAG, "addToCart: Product already exists, increasing quantity");
                // Increase quantity if stock allows
                int newQuantity = item.getQuantity() + 1;
                if (newQuantity <= product.getStock()) {
                    item.setQuantity(newQuantity);
                    item.setSubtotal(newQuantity * item.getPrice());
                    Log.d(TAG, "addToCart: Quantity increased to: " + item.getQuantity() + ", Subtotal: " + item.getSubtotal());
                } else {
                    Log.d(TAG, "addToCart: Cannot increase quantity, stock limit reached");
                    mainHandler.post(() -> {
                        errorMessage.setValue("Stok tidak mencukupi untuk produk " + product.getName());
                    });
                    return;
                }
                found = true;
                break;
            }
        }

        if (!found) {
            Log.d(TAG, "addToCart: Product not found, adding new item");
            // Check if product has stock
            if (product.getStock() > 0) {
                // Add new item to cart
                TransactionItem newItem = new TransactionItem(
                    0, // transactionId (akan diset saat checkout)
                    product.getId(), // productId
                    product.getName(), // productName
                    product.getPrice(), // price
                    1 // quantity
                );
                currentCart.add(newItem);
                Log.d(TAG, "addToCart: New item added, cart size now: " + currentCart.size());
            } else {
                Log.d(TAG, "addToCart: Product out of stock");
                mainHandler.post(() -> {
                    errorMessage.setValue("Produk " + product.getName() + " habis stok");
                });
                return;
            }
        }

        Log.d(TAG, "addToCart: Setting cart value with " + currentCart.size() + " items");
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
                    // Validate stock before updating quantity
                    productRepository.getProductById(cartItem.getProductId(), new ProductRepository.OnProductSearchListener() {
                        @Override
                        public void onSuccess(List<Product> products) {
                            if (!products.isEmpty()) {
                                Product product = products.get(0);
                                if (newQuantity <= product.getStock()) {
                                    cartItem.setQuantity(newQuantity);
                                    cartItem.setSubtotal(newQuantity * cartItem.getPrice());
                                    cartItems.setValue(currentCart);
                                } else {
                                    mainHandler.post(() -> {
                                        errorMessage.setValue("Stok tidak mencukupi. Stok tersedia: " + product.getStock());
                                    });
                                }
                            }
                        }

                        @Override
                        public void onError(String error) {
                            mainHandler.post(() -> {
                                errorMessage.setValue("Gagal memvalidasi stok: " + error);
                            });
                        }
                    });
                    break;
                }
            }
        }
    }

    public void clearCart() {
        cartItems.setValue(new ArrayList<>());
    }

    public void processTransaction(List<TransactionItem> items, double totalAmount) {
        processTransaction(items, totalAmount, totalAmount);
    }

    public void processTransaction(List<TransactionItem> items, double totalAmount, double amountPaid) {
        isLoading.setValue(true);
        
        // Create transaction with payment information
        Transaction transaction = new Transaction(totalAmount, "CASH");
        transaction.setAmountPaid(amountPaid);
        transaction.setChange(amountPaid - totalAmount);
        transaction.setStatus("COMPLETED"); // Set status to COMPLETED
        
        Log.d(TAG, "processTransaction: Total: " + totalAmount + ", Paid: " + amountPaid + ", Change: " + (amountPaid - totalAmount) + ", Status: COMPLETED");
        
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
                
                // Print receipt with payment information
                printReceipt(transaction, items);
                
                // Clear cart and refresh dashboard
                mainHandler.post(() -> {
                    clearCart();
                    isLoading.setValue(false);
                    
                    // Refresh dashboard data
                    refreshDashboardData();
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
    
    private void refreshDashboardData() {
        Log.d(TAG, "refreshDashboardData: Refreshing dashboard data after transaction");
        // Notify dashboard to refresh data
        // This will be handled by the Fragment observing the ViewModel
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
                                Log.d(TAG, "updateProductStock: Stock updated successfully for product " + product.getName() + 
                                      " (ID: " + product.getId() + "). Old stock: " + (product.getStock() + item.getQuantity()) + 
                                      ", New stock: " + product.getStock() + ", Quantity sold: " + item.getQuantity());
                            }

                            @Override
                            public void onError(String error) {
                                mainHandler.post(() -> {
                                    errorMessage.setValue("Gagal update stok: " + error);
                                });
                            }
                        });
                    } else {
                        Log.e(TAG, "updateProductStock: Cannot update stock - insufficient stock for product " + product.getName() + 
                              " (ID: " + product.getId() + "). Current stock: " + product.getStock() + ", Requested quantity: " + item.getQuantity());
                        mainHandler.post(() -> {
                            errorMessage.setValue("Stok tidak mencukupi untuk produk " + product.getName());
                        });
                    }
                } else {
                    Log.e(TAG, "updateProductStock: Product not found with ID: " + item.getProductId());
                    mainHandler.post(() -> {
                        errorMessage.setValue("Produk tidak ditemukan");
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "updateProductStock: Error getting product data: " + error);
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