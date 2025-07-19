package id.tugas.pos.ui.produk.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import id.tugas.pos.R;
import id.tugas.pos.data.model.Product;

public class AddEditProductDialog extends DialogFragment {

    private EditText etProductName, etProductCode, etProductCost, etProductPrice, etProductStock, etProductCategory, etProductUnit;
    private Button btnSave, btnCancel;
    private Product productToEdit;
    private OnProductSavedListener listener;
    private Integer storeId; // Tambahkan field untuk storeId
    private android.widget.TextView tvProfitMargin; // Tambahkan TextView untuk margin keuntungan

    public interface OnProductSavedListener {
        void onProductSaved(Product product);
    }

    public static AddEditProductDialog newInstance(Product product, Integer storeId) {
        AddEditProductDialog dialog = new AddEditProductDialog();
        dialog.productToEdit = product;
        dialog.storeId = storeId;
        return dialog;
    }

    public void setOnProductSavedListener(OnProductSavedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_edit_product, null);

        initViews(view);
        setupListeners();
        
        if (productToEdit != null) {
            // Edit mode
            populateFields();
            builder.setTitle("Edit Produk");
        } else {
            // Add mode
            builder.setTitle("Tambah Produk Baru");
        }

        builder.setView(view);
        return builder.create();
    }

    private void initViews(View view) {
        etProductName = view.findViewById(R.id.et_product_name);
        etProductCode = view.findViewById(R.id.et_product_code);
        etProductCost = view.findViewById(R.id.et_product_cost);
        etProductPrice = view.findViewById(R.id.et_product_price);
        etProductStock = view.findViewById(R.id.et_product_stock);
        etProductCategory = view.findViewById(R.id.et_product_category);
        etProductUnit = view.findViewById(R.id.et_product_unit);
        btnSave = view.findViewById(R.id.btn_save_product);
        btnCancel = view.findViewById(R.id.btn_cancel_product);
        tvProfitMargin = view.findViewById(R.id.tv_profit_margin);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveProduct());
        btnCancel.setOnClickListener(v -> dismiss());
        
        // Auto-calculate profit margin when cost or price changes
        etProductCost.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                calculateProfitMargin();
            }
        });
        
        etProductPrice.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                calculateProfitMargin();
            }
        });
    }
    
    private void calculateProfitMargin() {
        try {
            String costStr = etProductCost.getText().toString().trim();
            String priceStr = etProductPrice.getText().toString().trim();
            
            if (!TextUtils.isEmpty(costStr) && !TextUtils.isEmpty(priceStr)) {
                double cost = Double.parseDouble(costStr);
                double price = Double.parseDouble(priceStr);
                
                if (cost > 0 && price > cost) {
                    double profit = price - cost;
                    double margin = (profit / cost) * 100;
                    tvProfitMargin.setText(String.format("Margin: %.1f%% (Rp%.0f)", margin, profit));
                    tvProfitMargin.setTextColor(requireContext().getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    tvProfitMargin.setText("Margin: -");
                    tvProfitMargin.setTextColor(requireContext().getResources().getColor(android.R.color.holo_red_dark));
                }
            } else {
                tvProfitMargin.setText("Margin: -");
                tvProfitMargin.setTextColor(requireContext().getResources().getColor(android.R.color.darker_gray));
            }
        } catch (NumberFormatException e) {
            tvProfitMargin.setText("Margin: -");
            tvProfitMargin.setTextColor(requireContext().getResources().getColor(android.R.color.darker_gray));
        }
    }

    private void populateFields() {
        if (productToEdit != null) {
            etProductName.setText(productToEdit.getName());
            etProductCode.setText(productToEdit.getCode());
            etProductCost.setText(String.valueOf(productToEdit.getCostPrice()));
            etProductPrice.setText(String.valueOf(productToEdit.getPrice()));
            etProductStock.setText(String.valueOf(productToEdit.getStock()));
            etProductCategory.setText(productToEdit.getCategory());
            etProductUnit.setText(productToEdit.getUnit());
        }
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String code = etProductCode.getText().toString().trim();
        String costStr = etProductCost.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String stockStr = etProductStock.getText().toString().trim();
        String category = etProductCategory.getText().toString().trim();
        String unit = etProductUnit.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            etProductName.setError("Nama produk harus diisi");
            return;
        }

        if (TextUtils.isEmpty(code)) {
            etProductCode.setError("Kode produk harus diisi");
            return;
        }

        if (TextUtils.isEmpty(priceStr)) {
            etProductPrice.setError("Harga jual harus diisi");
            return;
        }

        if (TextUtils.isEmpty(costStr)) {
            etProductCost.setError("Harga modal harus diisi");
            return;
        }

        if (TextUtils.isEmpty(stockStr)) {
            etProductStock.setError("Stok harus diisi");
            return;
        }

        if (TextUtils.isEmpty(category)) {
            etProductCategory.setError("Kategori harus diisi");
            return;
        }

        if (TextUtils.isEmpty(unit)) {
            etProductUnit.setError("Satuan harus diisi");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            int stock = Integer.parseInt(stockStr);
            double cost = Double.parseDouble(costStr);

            if (price < 0) {
                etProductPrice.setError("Harga tidak boleh negatif");
                return;
            }

            if (stock < 0) {
                etProductStock.setError("Stok tidak boleh negatif");
                return;
            }

            if (cost < 0) {
                etProductCost.setError("Harga modal tidak boleh negatif");
                return;
            }

            if (price <= cost) {
                etProductPrice.setError("Harga jual harus lebih tinggi dari harga modal");
                return;
            }

            Product product;
            if (productToEdit != null) {
                // Update existing product
                product = new Product(
                    name,
                    code,
                    category,
                    price,
                    cost, // Use cost price
                    stock,
                    unit
                );
                product.setId(productToEdit.getId());
                product.setCreatedAt(productToEdit.getCreatedAt());
                product.setUpdatedAt(System.currentTimeMillis());
                product.setStoreId(productToEdit.getStoreId()); // Keep existing storeId
                android.util.Log.d("AddEditProductDialog", "Edit product - StoreId: " + product.getStoreId());
            } else {
                // Create new product
                product = new Product(
                    name,
                    code,
                    category,
                    price,
                    cost, // Use cost price
                    stock,
                    unit
                );
                // Set storeId for new product
                if (storeId != null) {
                    product.setStoreId(storeId);
                    android.util.Log.d("AddEditProductDialog", "New product - StoreId set to: " + storeId);
                } else {
                    android.util.Log.w("AddEditProductDialog", "New product - StoreId is null!");
                }
            }

            if (listener != null) {
                listener.onProductSaved(product);
            }
            dismiss();

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Format angka tidak valid", Toast.LENGTH_SHORT).show();
        }
    }
} 