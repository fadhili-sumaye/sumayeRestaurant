package com.example.sumayerestaurant.ui.waiter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.local.CartManager;
import com.example.sumayerestaurant.data.model.CartItem;
import com.example.sumayerestaurant.data.model.RestaurantTable;
import com.example.sumayerestaurant.ui.adapter.CartItemAdapter;
import com.example.sumayerestaurant.ui.viewmodel.CartViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private CartViewModel cartViewModel;
    private CartItemAdapter adapter;

    private TextView tvCartTableInfo;
    private TextView tvEmptyCart;
    private TextView tvSummarySubtotal;
    private TextView tvSummaryTotal;
    private EditText etOrderNotes;
    private ProgressBar progressBarSubmit;
    private MaterialButton btnSubmitOrder;
    private View layoutSubmitContainer;

    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupViewModel();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvCartTableInfo = findViewById(R.id.tvCartTableInfo);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        tvSummarySubtotal = findViewById(R.id.tvSummarySubtotal);
        tvSummaryTotal = findViewById(R.id.tvSummaryTotal);
        etOrderNotes = findViewById(R.id.etOrderNotes);
        progressBarSubmit = findViewById(R.id.progressBarSubmit);
        btnSubmitOrder = findViewById(R.id.btnSubmitOrder);
        layoutSubmitContainer = findViewById(R.id.layoutSubmitContainer);

        RestaurantTable table = CartManager.getInstance().getSelectedTable();
        if (table != null) {
            tvCartTableInfo.setText("Meza " + table.getTableNumber());
        } else {
            tvCartTableInfo.setText("Oda ya Kula Hapa");
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewCartItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartItemAdapter(this, this::showEditInstructionDialog);
        recyclerView.setAdapter(adapter);

        // Submit Order click
        btnSubmitOrder.setOnClickListener(v -> {
            String notes = etOrderNotes.getText().toString().trim();
            cartViewModel.submitOrder(notes);
        });
    }

    private void setupViewModel() {
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        cartViewModel.getCartItems().observe(this, items -> {
            adapter.setItems(items);
            boolean isEmpty = (items == null || items.isEmpty());
            tvEmptyCart.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            layoutSubmitContainer.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        cartViewModel.getSubtotal().observe(this, subtotal -> {
            String formatted = String.format("TZS %s", currencyFormat.format(subtotal != null ? subtotal : 0.0));
            tvSummarySubtotal.setText(formatted);
            tvSummaryTotal.setText(formatted);
        });

        // Double submission prevention & Progress
        cartViewModel.getIsSubmitting().observe(this, isSubmitting -> {
            btnSubmitOrder.setEnabled(!isSubmitting);
            btnSubmitOrder.setText(isSubmitting ? getString(R.string.submitting_order) : getString(R.string.submit_order));
            progressBarSubmit.setVisibility(isSubmitting ? View.VISIBLE : View.GONE);
        });

        cartViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        // Order Success
        cartViewModel.getOrderSuccess().observe(this, order -> {
            if (order != null) {
                Toast.makeText(this, getString(R.string.order_sent_to_kitchen), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(CartActivity.this, OrderDetailActivity.class);
                intent.putExtra("order", order);
                intent.putExtra("orderId", order.getId());
                startActivity(intent);
                finish();
            }
        });
    }

    private void showEditInstructionDialog(CartItem item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_special_instructions, null);
        TextView tvSubtitle = dialogView.findViewById(R.id.dialogItemSubtitle);
        EditText etInstructions = dialogView.findViewById(R.id.etSpecialInstructions);

        tvSubtitle.setText(item.getMenuItem().getName());
        etInstructions.setText(item.getSpecialInstructions());
        if (item.getSpecialInstructions() != null) {
            etInstructions.setSelection(item.getSpecialInstructions().length());
        }

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Hifadhi", (dialog, which) -> {
                    String note = etInstructions.getText().toString().trim();
                    cartViewModel.updateInstructions(item.getMenuItem().getId(), note);
                })
                .setNegativeButton("Ghairi", null)
                .show();
    }
}
