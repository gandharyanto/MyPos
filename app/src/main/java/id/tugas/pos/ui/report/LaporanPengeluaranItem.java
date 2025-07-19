package id.tugas.pos.ui.report;

public class LaporanPengeluaranItem {
    private String tanggal;
    private String kategori;
    private double nominal;
    private String keterangan;

    public LaporanPengeluaranItem(String tanggal, String kategori, double nominal, String keterangan) {
        this.tanggal = tanggal;
        this.kategori = kategori;
        this.nominal = nominal;
        this.keterangan = keterangan;
    }

    public String getTanggal() { return tanggal; }
    public String getKategori() { return kategori; }
    public double getNominal() { return nominal; }
    public String getKeterangan() { return keterangan; }
} 