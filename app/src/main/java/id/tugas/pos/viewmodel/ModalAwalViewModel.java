package id.tugas.pos.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import id.tugas.pos.data.model.ModalAwal;
import id.tugas.pos.data.repository.ModalAwalRepository;

public class ModalAwalViewModel extends AndroidViewModel {
    private final ModalAwalRepository repository;
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Double> saldoModal = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> modalHampirHabis = new MutableLiveData<>(false);

    public ModalAwalViewModel(@NonNull Application application) {
        super(application);
        repository = new ModalAwalRepository(application);
    }

    // LiveData Getters
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Double> getSaldoModal() { return saldoModal; }
    public LiveData<Boolean> getModalHampirHabis() { return modalHampirHabis; }

    // Tambah modal awal baru
    public void tambahModalAwal(int storeId, double nominal, String keterangan) {
        if (nominal <= 0) {
            message.setValue("Nominal harus lebih dari 0");
            return;
        }

        loading.setValue(true);
        repository.tambahModalAwal(storeId, nominal, keterangan, new ModalAwalRepository.Callback() {
            @Override
            public void onSuccess(long id) {
                loading.postValue(false);
                message.postValue("Modal awal berhasil ditambahkan");
                // Refresh saldo setelah menambah modal
                refreshSaldoModal(storeId);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue("Error: " + error);
            }
        });
    }

    // Refresh saldo modal saat ini
    public void refreshSaldoModal(int storeId) {
        new Thread(() -> {
            double saldo = repository.getSaldoModalSaatIni(storeId);
            saldoModal.postValue(saldo);
        }).start();
    }

    // Cek apakah modal hampir habis
    public void cekModalHampirHabis(int storeId, double batasMinimum) {
        repository.cekModalHampirsHabis(storeId, batasMinimum, new ModalAwalRepository.ModalHampirHabisCallback() {
            @Override
            public void onModalHampirHabis(double saldoSaatIni, double batasMinimum) {
                modalHampirHabis.postValue(true);
                saldoModal.postValue(saldoSaatIni);
                message.postValue(String.format("Modal hampir habis! Saldo: Rp %,.0f (Batas minimum: Rp %,.0f)",
                    saldoSaatIni, batasMinimum));
            }

            @Override
            public void onModalCukup(double saldoSaatIni) {
                modalHampirHabis.postValue(false);
                saldoModal.postValue(saldoSaatIni);
            }
        });
    }

    // Get modal awal hari ini
    public void getModalAwalToday(int storeId, ModalAwalTodayCallback callback) {
        new Thread(() -> {
            try {
                long today = Long.parseLong(new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()));
                ModalAwal modalToday = repository.getModalAwalToday(storeId, today);
                double totalHariIni = repository.getTotalPenambahanHariIni(storeId, today);
                callback.onResult(modalToday, totalHariIni);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // Get total modal berdasarkan store
    public void getTotalModal(int storeId, TotalModalCallback callback) {
        new Thread(() -> {
            try {
                double total = repository.getTotalModalByStore(storeId);
                callback.onResult(total);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // Get current modal (LiveData) - required by Fragment
    public LiveData<Double> getCurrentModal(int storeId) {
        MutableLiveData<Double> currentModal = new MutableLiveData<>();
        new Thread(() -> {
            double total = repository.getTotalModalByStore(storeId);
            currentModal.postValue(total);
        }).start();
        return currentModal;
    }

    // Get all modal awal (LiveData) - required by Fragment
    public LiveData<List<ModalAwal>> getAllModalAwal(int storeId) {
        return repository.getAllModalAwalByStoreLive(storeId);
    }

    // Load modal awal data - required by Fragment
    public void loadModalAwal(int storeId) {
        refreshSaldoModal(storeId);
        // Additional loading logic if needed
    }

    // Insert modal awal - required by Fragment
    public void insertModalAwal(ModalAwal modalAwal) {
        loading.setValue(true);
        repository.insert(modalAwal, new ModalAwalRepository.Callback() {
            @Override
            public void onSuccess(long id) {
                loading.postValue(false);
                message.postValue("Modal awal berhasil disimpan");
                // Refresh saldo setelah insert
                refreshSaldoModal(modalAwal.getStoreId());
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue("Error: " + error);
            }
        });
    }

    // Format currency untuk tampilan
    public String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "Rp %,.0f", amount);
    }

    // Callback interfaces
    public interface ModalAwalTodayCallback {
        void onResult(ModalAwal modalToday, double totalHariIni);
        void onError(String error);
    }

    public interface TotalModalCallback {
        void onResult(double total);
        void onError(String error);
    }
}
