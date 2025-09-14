package id.tugas.pos.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "modal_awal")
public class ModalAwal {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long tanggal; // format YYYYMMDD
    public int storeId;
    public double nominal; // nominal awal yang ditambahkan
    public double saldoSebelum; // saldo sebelum penambahan
    public double saldoSesudah; // saldo setelah penambahan
    public String tipe; // "INITIAL" atau "ADD_CAPITAL"
    public String keterangan;
    public long createdAt; // timestamp

    public ModalAwal() {
        this.createdAt = System.currentTimeMillis();
    }

    public ModalAwal(long tanggal, int storeId, double nominal, String tipe, String keterangan) {
        this.tanggal = tanggal;
        this.storeId = storeId;
        this.nominal = nominal;
        this.tipe = tipe;
        this.keterangan = keterangan;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTanggal() {
        return tanggal;
    }

    public void setTanggal(long tanggal) {
        this.tanggal = tanggal;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public double getNominal() {
        return nominal;
    }

    public void setNominal(double nominal) {
        this.nominal = nominal;
    }

    public double getSaldoSebelum() {
        return saldoSebelum;
    }

    public void setSaldoSebelum(double saldoSebelum) {
        this.saldoSebelum = saldoSebelum;
    }

    public double getSaldoSesudah() {
        return saldoSesudah;
    }

    public void setSaldoSesudah(double saldoSesudah) {
        this.saldoSesudah = saldoSesudah;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
