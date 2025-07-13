package id.tugas.pos.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {
    
    private static final DecimalFormat currencyFormatter = new DecimalFormat("#,##0");
    private static final NumberFormat indonesianFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    
    public static String formatCurrency(double amount) {
        return "Rp " + currencyFormatter.format(amount);
    }
    
    public static String formatCurrencyWithDecimal(double amount) {
        return indonesianFormat.format(amount);
    }
    
    public static String formatNumber(double number) {
        return currencyFormatter.format(number);
    }
    
    public static String formatNumber(int number) {
        return currencyFormatter.format(number);
    }
    
    public static double parseCurrency(String currencyString) {
        try {
            // Remove "Rp " prefix and any non-digit characters except decimal point
            String cleanString = currencyString.replace("Rp ", "").replace(",", "");
            return Double.parseDouble(cleanString);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    public static boolean isValidCurrency(String currencyString) {
        try {
            parseCurrency(currencyString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static String formatPercentage(double percentage) {
        return String.format("%.1f%%", percentage);
    }
    
    public static String formatQuantity(int quantity) {
        return String.valueOf(quantity);
    }
    
    public static String formatQuantity(double quantity) {
        if (quantity == (int) quantity) {
            return String.valueOf((int) quantity);
        } else {
            return String.format("%.2f", quantity);
        }
    }
} 