package id.tugas.pos.ui.transaksi.adapter;

import android.util.Log;
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

import java.util.List;

public class CartAdapter extends ListAdapter<TransactionItem, CartAdapter.CartViewHolder> {

    private static final String TAG = "CartAdapter";
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
        Log.d(TAG, "onCreateViewHolder: Creating new CartViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        Log.d(TAG, "onCreateViewHolder: View created successfully");
        return new CartViewHolder(view);
    }

    @Override
    public int getItemCount() {
        int count = super.getItemCount();
        Log.d(TAG, "getItemCount: " + count);
        return count;
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        TransactionItem item = getItem(position);
        Log.d(TAG, "onBindViewHolder: Binding item at position " + position + ": " + item.getName());
        holder.bind(item);
        Log.d(TAG, "onBindViewHolder: Binding completed for position " + position);
    }

    @Override
    public void submitList(List<TransactionItem> list) {
        Log.d(TAG, "submitList: Submitting list with " + (list != null ? list.size() : 0) + " items");
        
        // Debug: Log detail setiap item yang akan di-submit
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                TransactionItem item = list.get(i);
                Log.d(TAG, "submitList: Item " + i + ": ID=" + item.getProductId() + ", Name=" + item.getName() + ", Qty=" + item.getQuantity());
            }
        }
        
        super.submitList(list);
        
        // Debug: Check state after submit
        Log.d(TAG, "submitList: After super.submitList - getItemCount: " + getItemCount());
        
        // Force refresh if DiffUtil doesn't work
        if (list != null && !list.isEmpty() && getItemCount() > 0) {
            Log.d(TAG, "submitList: Forcing notifyDataSetChanged to ensure items are displayed");
            notifyDataSetChanged();
        }
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName;
        private TextView tvProductPrice;
        private TextView tvQuantity;
        private TextView tvSubtotal;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "CartViewHolder: Constructor called");
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvSubtotal = itemView.findViewById(R.id.tv_subtotal);
            Log.d(TAG, "CartViewHolder: Views found - Name: " + (tvProductName != null) + ", Price: " + (tvProductPrice != null) + ", Qty: " + (tvQuantity != null) + ", Subtotal: " + (tvSubtotal != null));

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
            Log.d(TAG, "CartViewHolder: Constructor completed");
        }

        public void bind(TransactionItem item) {
            Log.d(TAG, "bind: Setting data for item: " + item.getName() + ", Price: " + item.getPrice() + ", Qty: " + item.getQuantity());
            tvProductName.setText(item.getName());
            tvProductPrice.setText(CurrencyUtils.formatCurrency(item.getPrice()));
            tvQuantity.setText("x" + item.getQuantity());
            tvSubtotal.setText(CurrencyUtils.formatCurrency(item.getSubtotal()));
            Log.d(TAG, "bind: Data set successfully");
        }
    }

    private static final DiffUtil.ItemCallback<TransactionItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<TransactionItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransactionItem oldItem, @NonNull TransactionItem newItem) {
            // For cart items, we want to treat each item as unique based on productId
            boolean same = oldItem.getProductId() == newItem.getProductId();
            Log.d("CartAdapter", "areItemsTheSame: " + oldItem.getName() + " vs " + newItem.getName() + " = " + same);
            return same;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionItem oldItem, @NonNull TransactionItem newItem) {
            // For cart items, we want to update if quantity or price changes
            boolean same = oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getPrice() == newItem.getPrice() &&
                   oldItem.getQuantity() == newItem.getQuantity() &&
                   oldItem.getSubtotal() == newItem.getSubtotal();
            Log.d("CartAdapter", "areContentsTheSame: " + oldItem.getName() + " (Qty:" + oldItem.getQuantity() + ") vs " + newItem.getName() + " (Qty:" + newItem.getQuantity() + ") = " + same);
            return same;
        }
    };
} 