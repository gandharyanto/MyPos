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

public class LaporanStokAdapter extends RecyclerView.Adapter<LaporanStokAdapter.ViewHolder> {
    private List<LaporanStokItem> data = new ArrayList<>();

    public void setData(List<LaporanStokItem> newData) {
        data = newData != null ? newData : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<LaporanStokItem> getData() {
        return data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_laporan_stok, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LaporanStokItem item = data.get(position);
        holder.tvNamaProduk.setText(item.getNamaProduk());
        holder.tvStokMasuk.setText(String.valueOf(item.getStokMasuk()));
        holder.tvStokKeluar.setText(String.valueOf(item.getStokKeluar()));
        holder.tvStokTersisa.setText(String.valueOf(item.getStokTersisa()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaProduk, tvStokMasuk, tvStokKeluar, tvStokTersisa;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaProduk = itemView.findViewById(R.id.tvNamaProduk);
            tvStokMasuk = itemView.findViewById(R.id.tvStokMasuk);
            tvStokKeluar = itemView.findViewById(R.id.tvStokKeluar);
            tvStokTersisa = itemView.findViewById(R.id.tvStokTersisa);
        }
    }
}
