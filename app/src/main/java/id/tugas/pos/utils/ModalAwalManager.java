package id.tugas.pos.utils;

import android.app.Application;
import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import id.tugas.pos.data.repository.ModalAwalRepository;
import id.tugas.pos.data.repository.ExpenseRepository;

/**
 * Service untuk mengelola modal awal dan integrasinya dengan pengeluaran
 * Modal awal hanya berkurang ketika ada pengeluaran, sesuai kebutuhan bisnis
 */
public class ModalAwalManager {
    private final ModalAwalRepository modalAwalRepository;
    private final ExpenseRepository expenseRepository;
    private final MutableLiveData<Double> currentModalSaldo = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> isModalLow = new MutableLiveData<>(false);

    // Batas minimum modal (configurable)
    private double minModalThreshold = 100000; // Default: 100rb

    public ModalAwalManager(Context context) {
        this.modalAwalRepository = new ModalAwalRepository((Application) context.getApplicationContext());
        this.expenseRepository = new ExpenseRepository((Application) context.getApplicationContext());
    }

    /**
     * Mendapatkan saldo modal saat ini untuk store tertentu
     * Saldo = Total Modal Awal - Total Pengeluaran
     */
    public void refreshModalSaldo(int storeId) {
        new Thread(() -> {
            try {
                // Total modal yang telah ditambahkan
                double totalModal = modalAwalRepository.getTotalModalByStore(storeId);

                // Total pengeluaran (yang mengurangi modal)
                // Note: Implementasi ini mengasumsikan ExpenseRepository memiliki method untuk total by store
                // Jika belum ada, perlu ditambahkan
                double totalExpenses = getTotalExpensesByStore(storeId);

                // Saldo = Modal - Pengeluaran
                double saldoSaatIni = totalModal - totalExpenses;

                // Update LiveData
                currentModalSaldo.postValue(saldoSaatIni);

                // Cek apakah modal hampir habis
                isModalLow.postValue(saldoSaatIni <= minModalThreshold);

            } catch (Exception e) {
                currentModalSaldo.postValue(0.0);
                isModalLow.postValue(true);
            }
        }).start();
    }

    /**
     * Menambah modal awal baru
     */
    public void addModal(int storeId, double amount, String description, ModalCallback callback) {
        modalAwalRepository.tambahModalAwal(storeId, amount, description, new ModalAwalRepository.Callback() {
            @Override
            public void onSuccess(long id) {
                // Refresh saldo setelah menambah modal
                refreshModalSaldo(storeId);
                callback.onSuccess("Modal berhasil ditambahkan");
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Callback saat ada pengeluaran baru
     * Method ini dipanggil dari ExpenseFragment/ViewModel saat menambah pengeluaran
     */
    public void onExpenseAdded(int storeId, double expenseAmount) {
        // Refresh saldo modal karena ada pengeluaran baru
        refreshModalSaldo(storeId);

        // Cek apakah perlu peringatan modal rendah
        checkLowModalWarning(storeId);
    }

    /**
     * Cek dan berikan peringatan jika modal hampir habis
     */
    public void checkLowModalWarning(int storeId) {
        modalAwalRepository.cekModalHampirsHabis(storeId, minModalThreshold,
            new ModalAwalRepository.ModalHampirHabisCallback() {
                @Override
                public void onModalHampirHabis(double saldoSaatIni, double batasMinimum) {
                    isModalLow.postValue(true);
                    // Bisa ditambahkan notifikasi atau alert di sini
                }

                @Override
                public void onModalCukup(double saldoSaatIni) {
                    isModalLow.postValue(false);
                }
            });
    }

    /**
     * Set batas minimum modal
     */
    public void setMinModalThreshold(double threshold) {
        this.minModalThreshold = threshold;
    }

    /**
     * Get total expenses by store
     * Updated to use the proper ExpenseRepository method
     */
    private double getTotalExpensesByStore(int storeId) {
        try {
            return expenseRepository.getTotalExpensesByStoreSync(storeId);
        } catch (Exception e) {
            return 0.0;
        }
    }

    // LiveData Getters
    public LiveData<Double> getCurrentModalSaldo() {
        return currentModalSaldo;
    }

    public LiveData<Boolean> getIsModalLow() {
        return isModalLow;
    }

    // Callback interface
    public interface ModalCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}
