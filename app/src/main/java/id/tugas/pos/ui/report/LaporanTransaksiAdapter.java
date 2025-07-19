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

public class LaporanTransaksiAdapter extends RecyclerView.Adapter<LaporanTransaksiAdapter.ViewHolder> {
    private List<LaporanTransaksiItem> data = new ArrayList<>();

    public void setData(List<LaporanTransaksiItem> newData) {
        data = newData != null ? newData : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_laporan_transaksi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LaporanTransaksiItem item = data.get(position);
        holder.tvNamaProduk.setText(item.getNamaProduk());
        holder.tvJumlah.setText(String.valueOf(item.getJumlahTerjual()));
        holder.tvTotal.setText(String.format("Rp%,.0f", item.getTotalHarga()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaProduk, tvJumlah, tvTotal;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaProduk = itemView.findViewById(R.id.tvNamaProduk);
            tvJumlah = itemView.findViewById(R.id.tvJumlah);
            tvTotal = itemView.findViewById(R.id.tvTotal);
        }
    }
} 