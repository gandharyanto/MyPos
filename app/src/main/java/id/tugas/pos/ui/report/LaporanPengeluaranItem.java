package id.tugas.pos.ui.report;

import androidx.room.Ignore;

public class LaporanPengeluaranItem {
    private String tanggal;
    private String kategori;
    private double nominal;
    private String keterangan;
    @Ignore
    private double modalAwal;
    @Ignore
    private double sisaModal;

    // No-argument constructor for Room
    public LaporanPengeluaranItem() {
        this.modalAwal = 0.0;
        this.sisaModal = 0.0;
    }

    @Ignore
    public LaporanPengeluaranItem(String tanggal, String kategori, double nominal, String keterangan) {
        this.tanggal = tanggal;
        this.kategori = kategori;
        this.nominal = nominal;
        this.keterangan = keterangan;
        this.modalAwal = 0.0;
        this.sisaModal = 0.0;
    }

    // Constructor with modal information
    @Ignore
    public LaporanPengeluaranItem(String tanggal, String kategori, double nominal, String keterangan,
                                  double modalAwal, double sisaModal) {
        this.tanggal = tanggal;
        this.kategori = kategori;
        this.nominal = nominal;
        this.keterangan = keterangan;
        this.modalAwal = modalAwal;
        this.sisaModal = sisaModal;
    }

    public String getTanggal() { return tanggal; }
    public String getKategori() { return kategori; }
    public double getNominal() { return nominal; }
    public String getKeterangan() { return keterangan; }
    public double getModalAwal() { return modalAwal; }
    public double getSisaModal() { return sisaModal; }

    public void setTanggal(String tanggal) { this.tanggal = tanggal; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public void setNominal(double nominal) { this.nominal = nominal; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }
    public void setModalAwal(double modalAwal) { this.modalAwal = modalAwal; }
    public void setSisaModal(double sisaModal) { this.sisaModal = sisaModal; }
}
