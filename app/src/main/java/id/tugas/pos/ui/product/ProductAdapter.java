package id.tugas.pos.ui.product;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    
    private List<Product> products = new ArrayList<>();
    private OnProductClickListener listener;
    
    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onEditClick(Product product);
        void onDeleteClick(Product product);
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
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }
    
    @Override
    public int getItemCount() {
        return products.size();
    }
    
    class ProductViewHolder extends RecyclerView.ViewHolder {
        
        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvProductCode;
        private TextView tvPrice;
        private TextView tvStock;
        private TextView tvCategory;
        private ImageButton btnEdit;
        private ImageButton btnDelete;
        
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductCode = itemView.findViewById(R.id.tv_product_code);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            tvStock = itemView.findViewById(R.id.tv_product_stock);
            tvCategory = itemView.findViewById(R.id.tv_product_category);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductClick(products.get(position));
                }
            });
            
            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditClick(products.get(position));
                }
            });
            
            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteClick(products.get(position));
                }
            });
        }
        
        public void bind(Product product) {
            tvProductName.setText(product.getName());
            tvProductCode.setText(product.getCode());
            tvPrice.setText(CurrencyUtils.formatCurrency(product.getPrice()));
            tvStock.setText(String.valueOf(product.getStock()));
            tvCategory.setText(product.getCategory());
            
            // Load product image if available
            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImagePath())
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.ic_product_placeholder);
            }
            
            // Show stock warning if low
            if (product.getStock() <= product.getMinStock()) {
                tvStock.setTextColor(itemView.getContext().getResources().getColor(R.color.error));
            } else {
                tvStock.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));
            }
        }
    }
} 