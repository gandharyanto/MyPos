package id.tugas.pos.ui.report;

public class LaporanSavingItem {
    private String tanggal;
    private double nominal;
    private String keterangan;

    public LaporanSavingItem(String tanggal, double nominal, String keterangan) {
        this.tanggal = tanggal;
        this.nominal = nominal;
        this.keterangan = keterangan;
    }

    public String getTanggal() { return tanggal; }
    public double getNominal() { return nominal; }
    public String getKeterangan() { return keterangan; }
} 