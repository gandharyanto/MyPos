package id.tugas.pos.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {
    
    private static final Locale INDONESIA = new Locale("id", "ID");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(INDONESIA);
    private static final NumberFormat PERCENTAGE_FORMAT = NumberFormat.getPercentInstance(INDONESIA);
    
    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }
    
    public static String formatCurrency(long amount) {
        return CURRENCY_FORMAT.format(amount);
    }
    
    public static String formatPercentage(double percentage) {
        return PERCENTAGE_FORMAT.format(percentage / 100.0);
    }
    
    public static String formatNumber(double number) {
        return NumberFormat.getNumberInstance(INDONESIA).format(number);
    }
    
    public static String formatNumber(long number) {
        return NumberFormat.getNumberInstance(INDONESIA).format(number);
    }
    
    public static double parseCurrency(String currencyString) {
        try {
            // Remove currency symbol and non-numeric characters except decimal point
            String cleanString = currencyString.replaceAll("[^\\d.,]", "");
            // Replace comma with dot for decimal
            cleanString = cleanString.replace(",", ".");
            return Double.parseDouble(cleanString);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    public static String formatCompactCurrency(double amount) {
        if (amount >= 1_000_000_000) {
            return String.format("Rp%.1fM", amount / 1_000_000_000);
        } else if (amount >= 1_000_000) {
            return String.format("Rp%.1fJt", amount / 1_000_000);
        } else if (amount >= 1_000) {
            return String.format("Rp%.1fRb", amount / 1_000);
        } else {
            return formatCurrency(amount);
        }
    }
} 