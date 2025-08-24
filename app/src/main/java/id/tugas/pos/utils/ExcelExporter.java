package id.tugas.pos.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;
import android.os.Build;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import id.tugas.pos.data.model.Expense;
import id.tugas.pos.data.model.Product;
import id.tugas.pos.data.model.Transaction;
import id.tugas.pos.ui.report.LaporanTransaksiItem;
import id.tugas.pos.ui.report.LaporanPengeluaranItem;
import id.tugas.pos.ui.report.LaporanStokItem;

public class ExcelExporter {

    private static final String EXCEL_DIRECTORY = "MyPOS_Reports";

    public static void exportTransactions(Context context, List<Transaction> transactions) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Laporan Transaksi");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"No", "Tanggal", "Total", "Status", "Metode Pembayaran"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            for (int i = 0; i < transactions.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Transaction transaction = transactions.get(i);

                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(dateFormat.format(new Date(transaction.getCreatedAt())));

                Cell totalCell = row.createCell(2);
                totalCell.setCellValue(transaction.getTotal());
                totalCell.setCellStyle(currencyStyle);

                row.createCell(3).setCellValue(transaction.getStatus());
                row.createCell(4).setCellValue(transaction.getPaymentMethod());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            saveWorkbook(context, workbook, "Laporan_Transaksi");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Gagal mengekspor data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void exportExpenses(Context context, List<Expense> expenses) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Laporan Pengeluaran");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"No", "Tanggal", "Deskripsi", "Jumlah", "Kategori"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            for (int i = 0; i < expenses.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Expense expense = expenses.get(i);

                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(dateFormat.format(new Date(expense.getExpenseDate())));
                row.createCell(2).setCellValue(expense.getDescription());

                Cell amountCell = row.createCell(3);
                amountCell.setCellValue(expense.getAmount());
                amountCell.setCellStyle(currencyStyle);

                row.createCell(4).setCellValue(expense.getCategory());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            saveWorkbook(context, workbook, "Laporan_Pengeluaran");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Gagal mengekspor data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void exportStock(Context context, List<Product> products) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Laporan Stok");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"No", "Nama Produk", "Kategori", "Harga", "Stok", "Status"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            for (int i = 0; i < products.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Product product = products.get(i);

                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getCategory());

                Cell priceCell = row.createCell(3);
                priceCell.setCellValue(product.getPrice());
                priceCell.setCellStyle(currencyStyle);

                Cell stockCell = row.createCell(4);
                stockCell.setCellValue(product.getStock());
                stockCell.setCellStyle(numberStyle);

                String status = product.getStock() > 0 ? "Tersedia" : "Habis";
                row.createCell(5).setCellValue(status);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            saveWorkbook(context, workbook, "Laporan_Stok");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Gagal mengekspor data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void exportTransactionReport(Context context, List<LaporanTransaksiItem> items) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Laporan Transaksi");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"No", "Nama Produk", "Jumlah Terjual", "Total Harga"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            for (int i = 0; i < items.size(); i++) {
                Row row = sheet.createRow(i + 1);
                LaporanTransaksiItem item = items.get(i);

                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(item.getNamaProduk());

                Cell quantityCell = row.createCell(2);
                quantityCell.setCellValue(item.getJumlahTerjual());
                quantityCell.setCellStyle(numberStyle);

                Cell totalCell = row.createCell(3);
                totalCell.setCellValue(item.getTotalHarga());
                totalCell.setCellStyle(currencyStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            saveWorkbook(context, workbook, "Laporan_Transaksi_Produk");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Gagal mengekspor data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void exportExpenseReport(Context context, List<LaporanPengeluaranItem> items) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Laporan Pengeluaran");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"No", "Tanggal", "Kategori", "Nominal", "Keterangan"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            for (int i = 0; i < items.size(); i++) {
                Row row = sheet.createRow(i + 1);
                LaporanPengeluaranItem item = items.get(i);

                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(item.getTanggal());
                row.createCell(2).setCellValue(item.getKategori());

                Cell nominalCell = row.createCell(3);
                nominalCell.setCellValue(item.getNominal());
                nominalCell.setCellStyle(currencyStyle);

                row.createCell(4).setCellValue(item.getKeterangan());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            saveWorkbook(context, workbook, "Laporan_Pengeluaran");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Gagal mengekspor data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void exportStockReport(Context context, List<LaporanStokItem> items) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Laporan Stok");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"No", "Nama Produk", "Stok Masuk", "Stok Keluar", "Stok Tersisa"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            for (int i = 0; i < items.size(); i++) {
                Row row = sheet.createRow(i + 1);
                LaporanStokItem item = items.get(i);

                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(item.getNamaProduk());

                Cell stokMasukCell = row.createCell(2);
                stokMasukCell.setCellValue(item.getStokMasuk());
                stokMasukCell.setCellStyle(numberStyle);

                Cell stokKeluarCell = row.createCell(3);
                stokKeluarCell.setCellValue(item.getStokKeluar());
                stokKeluarCell.setCellStyle(numberStyle);

                Cell stokTersisaCell = row.createCell(4);
                stokTersisaCell.setCellValue(item.getStokTersisa());
                stokTersisaCell.setCellStyle(numberStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            saveWorkbook(context, workbook, "Laporan_Stok");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Gagal mengekspor data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        return headerStyle;
    }

    private static CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle currencyStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        currencyStyle.setDataFormat(format.getFormat("\"Rp \"#,##0"));
        return currencyStyle;
    }

    private static CellStyle createNumberStyle(Workbook workbook) {
        CellStyle numberStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        numberStyle.setDataFormat(format.getFormat("#,##0"));
        return numberStyle;
    }

    private static void saveWorkbook(Context context, Workbook workbook, String fileName) throws IOException {
        // Create timestamp for unique filename
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timestamp = dateFormat.format(new Date());
        String fullFileName = fileName + "_" + timestamp + ".xlsx";

        // Get Downloads directory
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File myPosDir = new File(downloadsDir, EXCEL_DIRECTORY);

        // Create directory if it doesn't exist
        if (!myPosDir.exists()) {
            myPosDir.mkdirs();
        }

        File file = new File(myPosDir, fullFileName);

        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
            workbook.close();

            Toast.makeText(context,
                "File Excel berhasil disimpan: " + file.getAbsolutePath(),
                Toast.LENGTH_LONG).show();
        }
    }
}
