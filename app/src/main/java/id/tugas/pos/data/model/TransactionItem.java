package id.tugas.pos.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "transaction_items",
        foreignKeys = @ForeignKey(entity = Transaction.class,
                parentColumns = "id",
                childColumns = "transactionId",
                onDelete = ForeignKey.CASCADE))
public class TransactionItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int transactionId;
    private int productId;
    private String productName;
    private double price;
    private int quantity;
    private double subtotal;
    private double discount;
    private double total;
    private long createdAt;

    public TransactionItem() {
        this.createdAt = System.currentTimeMillis();
    }

    public TransactionItem(int transactionId, int productId, String productName, double price, int quantity) {
        this.transactionId = transactionId;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.discount = 0;
        this.createdAt = System.currentTimeMillis();
        calculateTotals();
    }

    // Additional constructor for compatibility
    public TransactionItem(int transactionId, String productName, String productCode, double price, int quantity, double discount) {
        this.transactionId = transactionId;
        this.productId = 0; // Will be set later
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.discount = discount;
        this.createdAt = System.currentTimeMillis();
        calculateTotals();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        calculateTotals();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateTotals();
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
        calculateTotals();
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    private void calculateTotals() {
        this.subtotal = price * quantity;
        this.total = subtotal - discount;
    }

    public void incrementQuantity() {
        this.quantity++;
        calculateTotals();
    }

    public void decrementQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
            calculateTotals();
        }
    }

    // Alias method for compatibility
    public String getName() {
        return productName;
    }
}
