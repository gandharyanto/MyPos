package id.tugas.pos.ui.stockin;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import id.tugas.pos.R;
import id.tugas.pos.data.model.Product;
import id.tugas.pos.data.model.StockIn;
import id.tugas.pos.viewmodel.ProductViewModel;
import id.tugas.pos.viewmodel.StockInViewModel;
import java.util.List;

public class StockInDialogFragment extends DialogFragment {
    private AutoCompleteTextView actProduct;
    private EditText etQty;
    private Button btnSimpan;
    private ProductViewModel productViewModel;
    private StockInViewModel stockInViewModel;
    private List<Product> productList;
    private Product selectedProduct;

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_stock_in, null, false);
        actProduct = view.findViewById(R.id.actProduct);
        etQty = view.findViewById(R.id.etQty);
        btnSimpan = view.findViewById(R.id.btnSimpan);
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        stockInViewModel = new ViewModelProvider(requireActivity()).get(StockInViewModel.class);
        productViewModel.getAllProducts().observe(this, products -> {
            productList = products;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, 
                products.stream().map(Product::getName).toArray(String[]::new));
            actProduct.setAdapter(adapter);
        });
        actProduct.setOnItemClickListener((parent, view1, position, id) -> {
            selectedProduct = productList.get(position);
        });
        btnSimpan.setOnClickListener(v -> {
            if (selectedProduct == null) {
                Toast.makeText(getContext(), "Pilih produk", Toast.LENGTH_SHORT).show();
                return;
            }
            int qty = 0;
            try { qty = Integer.parseInt(etQty.getText().toString()); } catch (Exception ignored) {}
            if (qty <= 0) {
                Toast.makeText(getContext(), "Jumlah stok masuk harus > 0", Toast.LENGTH_SHORT).show();
                return;
            }
            StockIn stockIn = new StockIn();
            stockIn.productId = selectedProduct.getId();
            stockIn.productName = selectedProduct.getName();
            stockIn.quantity = qty;
            stockIn.createdAt = System.currentTimeMillis();
            stockInViewModel.insert(stockIn, () -> {
                Toast.makeText(getContext(), "Stok masuk berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        });
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);
        dialog.setTitle("Tambah Stok Masuk");
        return dialog;
    }
} 