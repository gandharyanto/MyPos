package id.tugas.pos.ui.modal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import id.tugas.pos.R;
import id.tugas.pos.data.model.ModalAwal;

public class ModalAwalAdapter extends RecyclerView.Adapter<ModalAwalAdapter.ViewHolder> {
    private List<ModalAwal> modalAwalList = new ArrayList<>();
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    public ModalAwalAdapter() {
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_modal_awal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModalAwal modalAwal = modalAwalList.get(position);
        holder.bind(modalAwal);
    }

    @Override
    public int getItemCount() {
        return modalAwalList.size();
    }

    public void setModalAwalList(List<ModalAwal> modalAwalList) {
        this.modalAwalList = modalAwalList != null ? modalAwalList : new ArrayList<>();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTanggal, tvTipe, tvNominal, tvSaldoSebelum, tvSaldoSesudah;
        private TextView tvKeterangan, tvCreatedAt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvTipe = itemView.findViewById(R.id.tvTipe);
            tvNominal = itemView.findViewById(R.id.tvNominal);
            tvSaldoSebelum = itemView.findViewById(R.id.tvSaldoSebelum);
            tvSaldoSesudah = itemView.findViewById(R.id.tvSaldoSesudah);
            tvKeterangan = itemView.findViewById(R.id.tvKeterangan);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
        }

        public void bind(ModalAwal modalAwal) {
            // Format tanggal
            Date tanggalDate = new Date(modalAwal.getTanggal());
            tvTanggal.setText(dateFormat.format(tanggalDate));

            // Set tipe dengan warna berbeda
            tvTipe.setText(modalAwal.getTipe().equals("INITIAL") ? "MODAL AWAL" : "TAMBAH MODAL");

            // Format nominal
            tvNominal.setText(currencyFormat.format(modalAwal.getNominal()));

            // Format saldo
            tvSaldoSebelum.setText(currencyFormat.format(modalAwal.getSaldoSebelum()));
            tvSaldoSesudah.setText(currencyFormat.format(modalAwal.getSaldoSesudah()));

            // Keterangan
            if (modalAwal.getKeterangan() != null && !modalAwal.getKeterangan().trim().isEmpty()) {
                tvKeterangan.setText(modalAwal.getKeterangan());
                tvKeterangan.setVisibility(View.VISIBLE);
            } else {
                tvKeterangan.setVisibility(View.GONE);
            }

            // Created at
            Date createdAtDate = new Date(modalAwal.getCreatedAt());
            SimpleDateFormat createdAtFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvCreatedAt.setText("Dibuat: " + createdAtFormat.format(createdAtDate));
        }
    }
}
