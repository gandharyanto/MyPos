package id.tugas.pos.ui.report;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import id.tugas.pos.R;

public class LaporanPengeluaranAdapter extends RecyclerView.Adapter<LaporanPengeluaranAdapter.ViewHolder> {
    private List<LaporanPengeluaranItem> data = new ArrayList<>();

    public void setData(List<LaporanPengeluaranItem> newData) {
        data = newData != null ? newData : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<LaporanPengeluaranItem> getData() {
        return data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_laporan_pengeluaran, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LaporanPengeluaranItem item = data.get(position);
        // Format epoch to dd/MM/yyyy
        try {
            long epoch = Long.parseLong(item.getTanggal());
            String formattedDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date(epoch));
            holder.tvTanggal.setText(formattedDate);
        } catch (Exception e) {
            holder.tvTanggal.setText(item.getTanggal());
        }
        holder.tvKategori.setText(item.getKategori());
        holder.tvNominal.setText(String.format("Rp%,.0f", item.getNominal()));
        holder.tvKeterangan.setText(item.getKeterangan());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTanggal, tvKategori, tvNominal, tvKeterangan;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvKategori = itemView.findViewById(R.id.tvKategori);
            tvNominal = itemView.findViewById(R.id.tvNominal);
            tvKeterangan = itemView.findViewById(R.id.tvKeterangan);
        }
    }
}
