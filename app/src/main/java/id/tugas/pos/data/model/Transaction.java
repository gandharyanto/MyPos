package id.tugas.pos.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String transactionNumber;
    private int userId;
    private String customerName;
    private double subtotal;
    private double tax;
    private double discount;
    private double total;
    private String paymentMethod; // "CASH", "CARD", "TRANSFER"
    private double cashReceived;
    private double change;
    private String status; // "PENDING", "COMPLETED", "CANCELLED"
    private String notes;
    private long createdAt;
    private long updatedAt;

    public Transaction() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.status = "PENDING";
        this.tax = 0;
        this.discount = 0;
        this.cashReceived = 0;
        this.change = 0;
    }

    public Transaction(String transactionNumber, int userId, String customerName, double subtotal) {
        this.transactionNumber = transactionNumber;
        this.userId = userId;
        this.customerName = customerName;
        this.subtotal = subtotal;
        this.total = subtotal;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.status = "PENDING";
        this.tax = 0;
        this.discount = 0;
        this.cashReceived = 0;
        this.change = 0;
    }

    // Additional constructor for compatibility
    public Transaction(double total, String paymentMethod) {
        this.transactionNumber = generateTransactionNumber();
        this.userId = 1; // Default user ID
        this.customerName = "";
        this.subtotal = total;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.status = "PENDING";
        this.tax = 0;
        this.discount = 0;
        this.cashReceived = 0;
        this.change = 0;
    }

    private String generateTransactionNumber() {
        return "TXN" + System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
        calculateTotal();
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
        calculateTotal();
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
        calculateTotal();
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getCashReceived() {
        return cashReceived;
    }

    public void setCashReceived(double cashReceived) {
        this.cashReceived = cashReceived;
        calculateChange();
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    private void calculateTotal() {
        this.total = subtotal + tax - discount;
    }

    private void calculateChange() {
        this.change = cashReceived - total;
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    // Alias methods for compatibility
    public double getTotalAmount() {
        return total;
    }

    public void setTotalAmount(double totalAmount) {
        this.total = totalAmount;
    }

    public double getAmountPaid() {
        return cashReceived;
    }

    public void setAmountPaid(double amountPaid) {
        this.cashReceived = amountPaid;
        calculateChange();
    }

    // Additional utility methods
    public String getFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(createdAt));
    }

    public String getCashierName() {
        return "User ID: " + userId; // For now, just return user ID
    }
} 