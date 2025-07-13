package id.tugas.pos.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import id.tugas.pos.R;
import id.tugas.pos.data.model.Transaction;
import id.tugas.pos.utils.CurrencyUtils;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.TransactionViewHolder> {
    
    private List<Transaction> transactions = new ArrayList<>();
    private OnTransactionClickListener listener;
    
    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }
    
    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }
    
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_history, parent, false);
        return new TransactionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }
    
    @Override
    public int getItemCount() {
        return transactions.size();
    }
    
    class TransactionViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvTransactionId;
        private TextView tvDate;
        private TextView tvAmount;
        private TextView tvPaymentMethod;
        private TextView tvStatus;
        
        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvTransactionId = itemView.findViewById(R.id.tvTransactionId);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTransactionClick(transactions.get(position));
                }
            });
        }
        
        public void bind(Transaction transaction) {
            tvTransactionId.setText("#" + transaction.getId());
            
            // Format date from timestamp
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            String formattedDate = sdf.format(new java.util.Date(transaction.getCreatedAt()));
            tvDate.setText(formattedDate);
            
            tvAmount.setText(CurrencyUtils.formatCurrency(transaction.getTotalAmount()));
            tvPaymentMethod.setText(transaction.getPaymentMethod());
            tvStatus.setText(transaction.getStatus());
            
            // Set status color
            if ("completed".equals(transaction.getStatus())) {
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.success));
            } else if ("pending".equals(transaction.getStatus())) {
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.warning));
            } else {
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.error));
            }
        }
    }
} 