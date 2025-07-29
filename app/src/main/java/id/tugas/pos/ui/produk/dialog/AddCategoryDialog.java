package id.tugas.pos.ui.produk.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import id.tugas.pos.R;
import id.tugas.pos.data.model.Category;
import id.tugas.pos.viewmodel.CategoryViewModel;
import id.tugas.pos.viewmodel.StoreViewModel;

public class AddCategoryDialog extends DialogFragment {

    private EditText etCategoryName;
    private Button btnSave, btnCancel;
    private OnCategorySavedListener listener;
    private CategoryViewModel categoryViewModel;
    private StoreViewModel storeViewModel;

    public interface OnCategorySavedListener {
        void onCategorySaved(String categoryName);
    }

    public static AddCategoryDialog newInstance() {
        return new AddCategoryDialog();
    }

    public void setOnCategorySavedListener(OnCategorySavedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_category, null);

        initViews(view);
        setupListeners();
        
        builder.setTitle("Tambah Kategori Baru");
        builder.setView(view);
        return builder.create();
    }

    private void initViews(View view) {
        etCategoryName = view.findViewById(R.id.et_category_name);
        btnSave = view.findViewById(R.id.btn_save_category);
        btnCancel = view.findViewById(R.id.btn_cancel_category);
        
        // Initialize ViewModels
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        storeViewModel = new ViewModelProvider(requireActivity()).get(StoreViewModel.class);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveCategory());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void saveCategory() {
        String categoryName = etCategoryName.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(categoryName)) {
            etCategoryName.setError("Nama kategori harus diisi");
            return;
        }

        if (categoryName.length() < 2) {
            etCategoryName.setError("Nama kategori minimal 2 karakter");
            return;
        }

        if (categoryName.length() > 50) {
            etCategoryName.setError("Nama kategori maksimal 50 karakter");
            return;
        }



        // Additional validation: check for special characters
        if (!categoryName.matches("^[a-zA-Z0-9\\s\\-]+$")) {
            etCategoryName.setError("Nama kategori hanya boleh berisi huruf, angka, spasi, dan tanda hubung");
            return;
        }

        // Save category to database
        saveCategoryToDatabase(categoryName);
    }
    
    private void saveCategoryToDatabase(String categoryName) {
        // Get current store ID
        Integer storeId = storeViewModel.getSelectedStoreId().getValue();
        if (storeId == null) {
            Toast.makeText(requireContext(), "Pilih toko terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if category already exists in database
        categoryViewModel.getCategoryByName(categoryName, storeId).observe(this, existingCategory -> {
            if (existingCategory != null) {
                etCategoryName.setError("Kategori ini sudah ada dalam database");
                return;
            }
            
            // Create new category
            Category newCategory = new Category(categoryName, "", storeId);
            categoryViewModel.insert(newCategory);
            
            Toast.makeText(requireContext(), "Kategori berhasil disimpan", Toast.LENGTH_SHORT).show();
            
            if (listener != null) {
                listener.onCategorySaved(categoryName);
            }
            dismiss();
        });
    }
} 