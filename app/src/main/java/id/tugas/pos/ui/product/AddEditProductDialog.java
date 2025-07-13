package id.tugas.pos.ui.product;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

import id.tugas.pos.R;
import id.tugas.pos.data.model.Product;

public class AddEditProductDialog extends DialogFragment {
    
    private static final int PICK_IMAGE_REQUEST = 1;
    
    private EditText etName, etCode, etPrice, etStock, etCategory;
    private Button btnSave, btnCancel;
    private TextView tvDialogTitle;
    
    private Product product;
    private String selectedImagePath;
    private OnProductSaveListener listener;
    
    public interface OnProductSaveListener {
        void onProductSave(Product product);
    }
    
    public static AddEditProductDialog newInstance(Product product) {
        AddEditProductDialog dialog = new AddEditProductDialog();
        dialog.product = product;
        return dialog;
    }
    
    public void setOnProductSaveListener(OnProductSaveListener listener) {
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
        
        if (product != null) {
            // Edit mode
            tvDialogTitle.setText("Edit Produk");
            populateFields();
        } else {
            // Add mode
            tvDialogTitle.setText("Tambah Produk");
            product = new Product();
        }
        
        builder.setView(view);
        return builder.create();
    }
    
    private void initViews(View view) {
        // Initialize views
        tvDialogTitle = view.findViewById(R.id.tv_dialog_title);
        etName = view.findViewById(R.id.et_product_name);
        etCode = view.findViewById(R.id.et_product_code);
        etPrice = view.findViewById(R.id.et_product_price);
        etStock = view.findViewById(R.id.et_product_stock);
        etCategory = view.findViewById(R.id.et_product_category);
        btnSave = view.findViewById(R.id.btn_save_product);
        btnCancel = view.findViewById(R.id.btn_cancel_product);
    }
    
    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveProduct());
        btnCancel.setOnClickListener(v -> dismiss());
    }
    
    private void populateFields() {
        etName.setText(product.getName());
        etCode.setText(product.getCode());
        etPrice.setText(String.valueOf(product.getPrice()));
        etStock.setText(String.valueOf(product.getStock()));
        etCategory.setText(product.getCategory());
        
        // Load image if available
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            selectedImagePath = product.getImagePath();
        }
    }
    
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    private void saveProduct() {
        // Validate input
        if (!validateInput()) {
            return;
        }
        
        // Get values from form
        String name = etName.getText().toString().trim();
        String code = etCode.getText().toString().trim();
        double price = Double.parseDouble(etPrice.getText().toString());
        int stock = Integer.parseInt(etStock.getText().toString());
        String category = etCategory.getText().toString().trim();
        
        // Update product
        product.setName(name);
        product.setCode(code);
        product.setPrice(price);
        product.setStock(stock);
        product.setCategory(category);
        product.setImagePath(selectedImagePath);
        
        if (listener != null) {
            listener.onProductSave(product);
        }
        
        dismiss();
    }
    
    private boolean validateInput() {
        String name = etName.getText().toString().trim();
        String code = etCode.getText().toString().trim();
        String priceStr = etPrice.getText().toString();
        String stockStr = etStock.getText().toString();
        String category = etCategory.getText().toString().trim();
        
        if (name.isEmpty()) {
            etName.setError("Nama produk harus diisi");
            return false;
        }
        
        if (code.isEmpty()) {
            etCode.setError("Kode produk harus diisi");
            return false;
        }
        
        if (priceStr.isEmpty()) {
            etPrice.setError("Harga harus diisi");
            return false;
        }
        
        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                etPrice.setError("Harga harus lebih dari 0");
                return false;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Harga harus berupa angka");
            return false;
        }
        
        if (stockStr.isEmpty()) {
            etStock.setError("Stok harus diisi");
            return false;
        }
        
        try {
            int stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                etStock.setError("Stok tidak boleh negatif");
                return false;
            }
        } catch (NumberFormatException e) {
            etStock.setError("Stok harus berupa angka");
            return false;
        }
        
        if (category.isEmpty()) {
            etCategory.setError("Kategori harus diisi");
            return false;
        }
        
        return true;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                selectedImagePath = selectedImageUri.toString();
                // Glide.with(this)
                //         .load(selectedImageUri)
                //         .placeholder(R.drawable.ic_product_placeholder)
                //         .into(ivProductImage);
            }
        }
    }
} 