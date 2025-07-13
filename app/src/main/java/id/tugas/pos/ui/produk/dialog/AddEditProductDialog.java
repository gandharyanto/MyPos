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

    private EditText etProductName, etProductCode, etProductPrice, etProductStock, etProductCategory;
    private Button btnSave, btnCancel;
    private Product productToEdit;
    private OnProductSavedListener listener;

    public interface OnProductSavedListener {
        void onProductSaved(Product product);
    }

    public static AddEditProductDialog newInstance(Product product) {
        AddEditProductDialog dialog = new AddEditProductDialog();
        dialog.productToEdit = product;
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
        etProductPrice = view.findViewById(R.id.et_product_price);
        etProductStock = view.findViewById(R.id.et_product_stock);
        etProductCategory = view.findViewById(R.id.et_product_category);
        btnSave = view.findViewById(R.id.btn_save_product);
        btnCancel = view.findViewById(R.id.btn_cancel_product);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveProduct());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void populateFields() {
        if (productToEdit != null) {
            etProductName.setText(productToEdit.getName());
            etProductCode.setText(productToEdit.getCode());
            etProductPrice.setText(String.valueOf(productToEdit.getPrice()));
            etProductStock.setText(String.valueOf(productToEdit.getStock()));
            etProductCategory.setText(productToEdit.getCategory());
        }
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String code = etProductCode.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String stockStr = etProductStock.getText().toString().trim();
        String category = etProductCategory.getText().toString().trim();

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
            etProductPrice.setError("Harga harus diisi");
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

        try {
            double price = Double.parseDouble(priceStr);
            int stock = Integer.parseInt(stockStr);

            if (price < 0) {
                etProductPrice.setError("Harga tidak boleh negatif");
                return;
            }

            if (stock < 0) {
                etProductStock.setError("Stok tidak boleh negatif");
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
                    price * 0.7, // Default cost price
                    stock,
                    "pcs" // Default unit
                );
                product.setId(productToEdit.getId());
                product.setCreatedAt(productToEdit.getCreatedAt());
                product.setUpdatedAt(System.currentTimeMillis());
            } else {
                // Create new product
                product = new Product(
                    name,
                    code,
                    category,
                    price,
                    price * 0.7, // Default cost price
                    stock,
                    "pcs" // Default unit
                );
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