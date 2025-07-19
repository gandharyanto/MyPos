package id.tugas.pos.ui.report;

public class LaporanTransaksiItem {
    private String namaProduk;
    private int jumlahTerjual;
    private double totalHarga;

    public LaporanTransaksiItem(String namaProduk, int jumlahTerjual, double totalHarga) {
        this.namaProduk = namaProduk;
        this.jumlahTerjual = jumlahTerjual;
        this.totalHarga = totalHarga;
    }

    public String getNamaProduk() { return namaProduk; }
    public int getJumlahTerjual() { return jumlahTerjual; }
    public double getTotalHarga() { return totalHarga; }
} 