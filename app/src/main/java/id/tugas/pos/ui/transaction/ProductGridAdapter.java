package id.tugas.pos.ui.transaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import id.tugas.pos.R;
import id.tugas.pos.data.model.Product;
import id.tugas.pos.utils.CurrencyUtils;

public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.ProductGridViewHolder> {
    
    private List<Product> products = new ArrayList<>();
    private OnProductClickListener listener;
    
    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
    
    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }
    
    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ProductGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_grid, parent, false);
        return new ProductGridViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ProductGridViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }
    
    @Override
    public int getItemCount() {
        return products.size();
    }
    
    class ProductGridViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvProductName;
        private TextView tvPrice;
        private TextView tvStock;
        
        public ProductGridViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            tvStock = itemView.findViewById(R.id.tv_product_stock);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Product product = products.get(position);
                    if (product.getStock() > 0) {
                        listener.onProductClick(product);
                    }
                }
            });
        }
        
        public void bind(Product product) {
            tvProductName.setText(product.getName());
            tvPrice.setText(CurrencyUtils.formatCurrency(product.getPrice()));
            tvStock.setText("Stok: " + product.getStock());
            
            // Show stock status
            if (product.getStock() <= 0) {
                itemView.setAlpha(0.5f);
                itemView.setEnabled(false);
            } else if (product.getStock() <= product.getMinStock()) {
                itemView.setAlpha(0.8f);
                itemView.setEnabled(true);
            } else {
                itemView.setAlpha(1.0f);
                itemView.setEnabled(true);
            }
        }
    }
} 