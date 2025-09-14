package id.tugas.pos.data.repository;

import java.util.List;
import id.tugas.pos.ui.report.LaporanPengeluaranItem;

public interface LaporanCallback {
    void onSuccess(List<LaporanPengeluaranItem> laporan);
    void onError(String errorMessage);
}
