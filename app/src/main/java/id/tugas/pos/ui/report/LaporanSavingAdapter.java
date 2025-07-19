package id.tugas.pos.ui.report;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import id.tugas.pos.R;

public class LaporanSavingAdapter extends RecyclerView.Adapter<LaporanSavingAdapter.ViewHolder> {
    private List<LaporanSavingItem> data = new ArrayList<>();

    public void setData(List<LaporanSavingItem> newData) {
        data = newData != null ? newData : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_laporan_saving, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LaporanSavingItem item = data.get(position);
        holder.tvTanggal.setText(item.getTanggal());
        holder.tvNominal.setText(String.format("Rp%,.0f", item.getNominal()));
        holder.tvKeterangan.setText(item.getKeterangan());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTanggal, tvNominal, tvKeterangan;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvNominal = itemView.findViewById(R.id.tvNominal);
            tvKeterangan = itemView.findViewById(R.id.tvKeterangan);
        }
    }
} 