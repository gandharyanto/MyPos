package id.tugas.pos.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import id.tugas.pos.data.model.Transaction;
import id.tugas.pos.data.model.TransactionItem;

public class PrinterUtils {
    
    private static final String TAG = "PrinterUtils";
    
    public static void printReceipt(Context context, Transaction transaction, List<TransactionItem> items, String printerAddress) {
        try {
            DeviceConnection printerConnection = BluetoothPrintersConnections.selectFirstPaired();
            if (printerConnection != null) {
                EscPosPrinter printer = new EscPosPrinter(printerConnection, 203, 48f, 32);
                
                String receipt = generateReceiptText(transaction, items);
                printer.printFormattedText(receipt);
                printer.disconnectPrinter();
            } else {
                Log.e(TAG, "No printer connected");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error printing receipt: " + e.getMessage());
        }
    }
    
    private static String generateReceiptText(Transaction transaction, List<TransactionItem> items) {
        StringBuilder receipt = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        
        // Header
        receipt.append("[C]<b>KASIR KAS KECIL</b>\n");
        receipt.append("[C]Jl. Contoh No. 123\n");
        receipt.append("[C]Telp: (021) 1234567\n");
        receipt.append("[C]================================\n\n");
        
        // Transaction info
        receipt.append("[L]No. Transaksi: ").append(transaction.getTransactionNumber()).append("\n");
        receipt.append("[L]Tanggal: ").append(dateFormat.format(new Date(transaction.getCreatedAt()))).append("\n");
        receipt.append("[L]Kasir: ").append("User ID: ").append(transaction.getUserId()).append("\n");
        if (transaction.getCustomerName() != null && !transaction.getCustomerName().isEmpty()) {
            receipt.append("[L]Customer: ").append(transaction.getCustomerName()).append("\n");
        }
        receipt.append("[C]================================\n\n");
        
        // Items
        receipt.append("[L]<b>ITEM</b>[R]<b>QTY</b>[R]<b>HARGA</b>[R]<b>TOTAL</b>\n");
        receipt.append("[C]--------------------------------\n");
        
        for (TransactionItem item : items) {
            String itemName = item.getProductName();
            if (itemName.length() > 20) {
                itemName = itemName.substring(0, 17) + "...";
            }
            
            receipt.append("[L]").append(itemName).append("\n");
            receipt.append("[L]  ").append(item.getQuantity()).append(" x ")
                   .append(CurrencyUtils.formatCurrency(item.getPrice())).append("\n");
            receipt.append("[R]").append(CurrencyUtils.formatCurrency(item.getTotal())).append("\n\n");
        }
        
        receipt.append("[C]================================\n");
        
        // Totals
        receipt.append("[L]Subtotal:[R]").append(CurrencyUtils.formatCurrency(transaction.getSubtotal())).append("\n");
        if (transaction.getTax() > 0) {
            receipt.append("[L]Pajak:[R]").append(CurrencyUtils.formatCurrency(transaction.getTax())).append("\n");
        }
        if (transaction.getDiscount() > 0) {
            receipt.append("[L]Diskon:[R]").append(CurrencyUtils.formatCurrency(transaction.getDiscount())).append("\n");
        }
        receipt.append("[L]<b>TOTAL:[R]").append(CurrencyUtils.formatCurrency(transaction.getTotal())).append("</b>\n");
        
        if (transaction.getPaymentMethod() != null) {
            receipt.append("[L]Metode Pembayaran: ").append(transaction.getPaymentMethod()).append("\n");
        }
        
        if (transaction.getCashReceived() > 0) {
            receipt.append("[L]Tunai:[R]").append(CurrencyUtils.formatCurrency(transaction.getCashReceived())).append("\n");
            receipt.append("[L]Kembalian:[R]").append(CurrencyUtils.formatCurrency(transaction.getChange())).append("\n");
        }
        
        receipt.append("[C]================================\n");
        receipt.append("[C]Terima kasih atas kunjungan Anda\n");
        receipt.append("[C]Barang yang sudah dibeli tidak dapat dikembalikan\n");
        receipt.append("[C]================================\n\n\n\n");
        
        return receipt.toString();
    }
    
    public static void printTestPage(Context context, String printerAddress) {
        try {
            DeviceConnection printerConnection = BluetoothPrintersConnections.selectFirstPaired();
            if (printerConnection != null) {
                EscPosPrinter printer = new EscPosPrinter(printerConnection, 203, 48f, 32);
                
                String testText = "[C]<b>TEST PRINTER</b>\n";
                testText += "[C]KASIR KAS KECIL\n";
                testText += "[C]================================\n";
                testText += "[L]Tanggal: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()) + "\n";
                testText += "[L]Status: Printer berfungsi dengan baik\n";
                testText += "[C]================================\n\n\n\n";
                
                printer.printFormattedText(testText);
                printer.disconnectPrinter();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error printing test page: " + e.getMessage());
        }
    }
    
    public static boolean isPrinterConnected() {
        try {
            DeviceConnection printerConnection = BluetoothPrintersConnections.selectFirstPaired();
            return printerConnection != null;
        } catch (Exception e) {
            Log.e(TAG, "Error checking printer connection: " + e.getMessage());
            return false;
        }
    }
} 