package id.tugas.pos.ui.expense;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import id.tugas.pos.R;
import id.tugas.pos.data.model.Expense;
import id.tugas.pos.viewmodel.ExpenseViewModel;
import id.tugas.pos.viewmodel.LoginViewModel;
import id.tugas.pos.data.model.User;

public class ExpenseDialogFragment extends DialogFragment {
    private EditText etTitle, etDescription, etAmount;
    private Spinner spinnerCategory, spinnerPaymentMethod;
    private Button btnSave, btnCancel;
    private ExpenseViewModel expenseViewModel;
    private LoginViewModel loginViewModel;
    private int storeId;
    private int userId;

    public ExpenseDialogFragment(int storeId) {
        this.storeId = storeId;
    }

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (storeId <= 0) {
            Toast.makeText(requireContext(), "Pilih toko terlebih dahulu!", Toast.LENGTH_SHORT).show();
            dismiss();
            return super.onCreateDialog(savedInstanceState);
        }

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_expense, null, false);
        
        // Initialize views
        etTitle = view.findViewById(R.id.etExpenseTitle);
        etDescription = view.findViewById(R.id.etExpenseDescription);
        etAmount = view.findViewById(R.id.etExpenseAmount);
        spinnerCategory = view.findViewById(R.id.spinnerExpenseCategory);
        spinnerPaymentMethod = view.findViewById(R.id.spinnerPaymentMethod);
        btnSave = view.findViewById(R.id.btnSaveExpense);
        btnCancel = view.findViewById(R.id.btnCancelExpense);

        // Initialize ViewModels
        expenseViewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        // Setup spinners
        setupCategorySpinner();
        setupPaymentMethodSpinner();

        // Setup listeners
        btnSave.setOnClickListener(v -> saveExpense());
        btnCancel.setOnClickListener(v -> dismiss());

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);
        dialog.setTitle("Tambah Pengeluaran");
        return dialog;
    }

    private void setupCategorySpinner() {
        String[] categories = {"OPERATIONAL"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_black_text, categories);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_black_text);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupPaymentMethodSpinner() {
        String[] paymentMethods = {"CASH", "BANK TRANSFER", "CREDIT CARD", "OTHER"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_black_text, paymentMethods);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_black_text);
        spinnerPaymentMethod.setAdapter(adapter);
    }

    private void saveExpense() {
        // Disable button to prevent multiple clicks
        btnSave.setEnabled(false);

        // Get input values
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String paymentMethod = spinnerPaymentMethod.getSelectedItem().toString();

        // Validation
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Judul pengeluaran harus diisi");
            btnSave.setEnabled(true);
            return;
        }

        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Jumlah pengeluaran harus diisi");
            btnSave.setEnabled(true);
            return;
        }

        double amount = 0;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Format jumlah tidak valid");
            btnSave.setEnabled(true);
            return;
        }

        if (amount <= 0) {
            etAmount.setError("Jumlah harus lebih dari 0");
            btnSave.setEnabled(true);
            return;
        }

        // Get current user ID
        User currentUser = loginViewModel.getCurrentUser().getValue();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User tidak ditemukan", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true);
            return;
        }

        // Create expense object
        Expense expense = new Expense();
        expense.setTitle(title);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setPaymentMethod(paymentMethod);
        expense.setUserId(currentUser.getId());
        expense.setStoreId(storeId);
        expense.setExpenseDate(System.currentTimeMillis());

        // Save to database
        expenseViewModel.insert(expense, () -> {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Pengeluaran berhasil disimpan", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        });
    }
} 