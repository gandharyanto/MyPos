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
import id.tugas.pos.viewmodel.ProductViewModel;
import id.tugas.pos.viewmodel.CategoryViewModel;
import androidx.lifecycle.ViewModelProvider;

public class AddEditProductDialog extends DialogFragment {

    private EditText etProductName, etProductCode, etProductCost, etProductPrice, etProductStock, etProductUnit;
    private android.widget.Spinner spinnerCategory;
    private Button btnSave, btnCancel;
    private Product productToEdit;
    private OnProductSavedListener listener;
    private Integer storeId; // Tambahkan field untuk storeId
    private android.widget.TextView tvProfitMargin; // Tambahkan TextView untuk margin keuntungan
    private ProductViewModel productViewModel;
    private CategoryViewModel categoryViewModel;
    
    // Image related fields
    private android.widget.ImageView ivProductImage;
    private android.widget.Button btnCamera, btnGallery;
    private String selectedImagePath;
    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_GALLERY = 1002;

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
        spinnerCategory = view.findViewById(R.id.spinner_product_category);
        etProductUnit = view.findViewById(R.id.et_product_unit);
        btnSave = view.findViewById(R.id.btn_save_product);
        btnCancel = view.findViewById(R.id.btn_cancel_product);
        tvProfitMargin = view.findViewById(R.id.tv_profit_margin);
        
        // Initialize image views
        ivProductImage = view.findViewById(R.id.iv_product_image);
        btnCamera = view.findViewById(R.id.btn_camera);
        btnGallery = view.findViewById(R.id.btn_gallery);
        
        // Initialize ViewModels
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        
        // Setup category spinner
        setupCategorySpinner();
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveProduct());
        btnCancel.setOnClickListener(v -> dismiss());
        
        // Image selection listeners
        btnCamera.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());
        
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
    
    private void setupCategorySpinner() {
        // Get categories from database for this store
        if (storeId != null) {
            categoryViewModel.getAllCategoryNamesByStore(storeId).observe(this, dbCategories -> {
                if (dbCategories != null && !dbCategories.isEmpty()) {
                    // Create adapter with database categories only
                    android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                        requireContext(),
                        R.layout.spinner_item_black_text,
                        dbCategories
                    );
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_black_text);
                    spinnerCategory.setAdapter(adapter);
                    
                    // Set default selection to first item
                    if (dbCategories.size() > 0) {
                        spinnerCategory.setSelection(0);
                    }
                } else {
                    // If no categories in database, show empty spinner
                    android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                        requireContext(),
                        R.layout.spinner_item_black_text,
                        new java.util.ArrayList<>()
                    );
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_black_text);
                    spinnerCategory.setAdapter(adapter);
                }
            });
        } else {
            // If no storeId, show empty spinner
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_item_black_text,
                new java.util.ArrayList<>()
            );
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_black_text);
            spinnerCategory.setAdapter(adapter);
        }
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
            
            // Set category spinner to match product category
            String productCategory = productToEdit.getCategory();
            android.widget.ArrayAdapter<String> adapter = (android.widget.ArrayAdapter<String>) spinnerCategory.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(productCategory)) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
            
            etProductUnit.setText(productToEdit.getUnit());
            
            // Load existing product image
            if (productToEdit.getImagePath() != null && !productToEdit.getImagePath().isEmpty()) {
                loadProductImage(productToEdit.getImagePath());
                selectedImagePath = productToEdit.getImagePath();
            }
        }
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String code = etProductCode.getText().toString().trim();
        String costStr = etProductCost.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String stockStr = etProductStock.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
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
                // Set image path if selected, otherwise keep existing
                if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                    product.setImagePath(selectedImagePath);
                } else {
                    product.setImagePath(productToEdit.getImagePath());
                }
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
                // Set image path if selected
                if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                    product.setImagePath(selectedImagePath);
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
    
    // Image handling methods
    private void openCamera() {
        if (checkCameraPermission()) {
            android.content.Intent intent = new android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }
    
    private void openGallery() {
        if (checkStoragePermission()) {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_GALLERY);
        }
    }
    
    private boolean checkCameraPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (requireActivity().checkSelfPermission(android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA);
                return false;
            }
        }
        return true;
    }
    
    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (requireActivity().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY);
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_CAMERA) {
                openCamera();
            } else if (requestCode == REQUEST_GALLERY) {
                openGallery();
            }
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == android.app.Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA && data != null) {
                android.graphics.Bitmap photo = (android.graphics.Bitmap) data.getExtras().get("data");
                if (photo != null) {
                    selectedImagePath = saveImageToInternalStorage(photo);
                    ivProductImage.setImageBitmap(photo);
                }
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                android.net.Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    selectedImagePath = getRealPathFromURI(selectedImage);
                    ivProductImage.setImageURI(selectedImage);
                }
            }
        }
    }
    
    private String saveImageToInternalStorage(android.graphics.Bitmap bitmap) {
        try {
            java.io.File directory = new java.io.File(requireContext().getFilesDir(), "product_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            String fileName = "product_" + System.currentTimeMillis() + ".jpg";
            java.io.File file = new java.io.File(directory, fileName);
            
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private String getRealPathFromURI(android.net.Uri contentUri) {
        try {
            String[] proj = {android.provider.MediaStore.Images.Media.DATA};
            android.database.Cursor cursor = requireContext().getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(column_index);
                cursor.close();
                return path;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void loadProductImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                java.io.File file = new java.io.File(imagePath);
                if (file.exists()) {
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(file.getAbsolutePath());
                    ivProductImage.setImageBitmap(bitmap);
                    selectedImagePath = imagePath;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
} 