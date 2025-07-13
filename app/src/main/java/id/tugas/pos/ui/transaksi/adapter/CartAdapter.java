package id.tugas.pos.ui.transaksi.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.tugas.pos.R;
import id.tugas.pos.data.model.TransactionItem;
import id.tugas.pos.utils.CurrencyUtils;

public class CartAdapter extends ListAdapter<TransactionItem, CartAdapter.CartViewHolder> {

    private OnCartItemClickListener listener;

    public interface OnCartItemClickListener {
        void onCartItemClick(TransactionItem item);
        void onCartItemLongClick(TransactionItem item);
    }

    public CartAdapter(OnCartItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        TransactionItem item = getItem(position);
        holder.bind(item);
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName;
        private TextView tvProductPrice;
        private TextView tvQuantity;
        private TextView tvSubtotal;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvSubtotal = itemView.findViewById(R.id.tv_subtotal);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCartItemClick(getItem(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCartItemLongClick(getItem(position));
                    return true;
                }
                return false;
            });
        }

        public void bind(TransactionItem item) {
            tvProductName.setText(item.getName());
            tvProductPrice.setText(CurrencyUtils.formatCurrency(item.getPrice()));
            tvQuantity.setText("x" + item.getQuantity());
            tvSubtotal.setText(CurrencyUtils.formatCurrency(item.getSubtotal()));
        }
    }

    private static final DiffUtil.ItemCallback<TransactionItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<TransactionItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransactionItem oldItem, @NonNull TransactionItem newItem) {
            return oldItem.getProductId() == newItem.getProductId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionItem oldItem, @NonNull TransactionItem newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getPrice() == newItem.getPrice() &&
                   oldItem.getQuantity() == newItem.getQuantity() &&
                   oldItem.getSubtotal() == newItem.getSubtotal();
        }
    };
} 