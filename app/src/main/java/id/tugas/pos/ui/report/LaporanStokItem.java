package id.tugas.pos.ui.report;

public class LaporanStokItem {
    private String namaProduk;
    private int stokMasuk;
    private int stokKeluar;
    private int stokTersisa;

    public LaporanStokItem(String namaProduk, int stokMasuk, int stokKeluar, int stokTersisa) {
        this.namaProduk = namaProduk;
        this.stokMasuk = stokMasuk;
        this.stokKeluar = stokKeluar;
        this.stokTersisa = stokTersisa;
    }

    public String getNamaProduk() { return namaProduk; }
    public int getStokMasuk() { return stokMasuk; }
    public int getStokKeluar() { return stokKeluar; }
    public int getStokTersisa() { return stokTersisa; }

    public void setStokKeluar(int stokKeluar) { this.stokKeluar = stokKeluar; }
    public void setStokMasuk(int stokMasuk) { this.stokMasuk = stokMasuk; }
} 