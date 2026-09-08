package com.example.sumayerestaurant.ui.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.local.CartManager;
import com.example.sumayerestaurant.data.model.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Local shopping cart screen. Lists items the customer added with quantity
 * controls, shows the running total and lets the customer clear the cart.
 */
public class CustomerCartActivity extends AppCompatActivity {

    private CartManager cartManager;
    private CartAdapter cartAdapter;
    private RecyclerView cartRecyclerView;
    private View emptyLayout;
    private TextView totalTextView;
    private TextView itemCountTextView;
    private View summaryCard;
    private View btnClear;
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_cart);

        cartManager = CartManager.getInstance();

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        emptyLayout = findViewById(R.id.emptyLayout);
        totalTextView = findViewById(R.id.totalTextView);
        itemCountTextView = findViewById(R.id.itemCountTextView);
        summaryCard = findViewById(R.id.summaryCard);
        btnClear = findViewById(R.id.btnClear);

        cartAdapter = new CartAdapter(this, cartManager);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartAdapter);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        btnClear.setOnClickListener(v -> {
            cartManager.clearCart();
            Toast.makeText(this, "Kikapu kimefutwa", Toast.LENGTH_SHORT).show();
        });

        cartManager.getCartLiveData().observe(this, this::renderCart);
    }

    private void renderCart(List<CartItem> cartItems) {
        List<CartItem> items = cartItems != null ? cartItems : new ArrayList<>();
        cartAdapter.setItems(items);

        int count = 0;
        double subtotal = 0.0;
        for (CartItem item : items) {
            count += item.getQuantity();
            subtotal += item.getSubtotal();
        }

        boolean empty = items.isEmpty();
        cartRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyLayout.setVisibility(empty ? View.VISIBLE : View.GONE);
        summaryCard.setVisibility(empty ? View.GONE : View.VISIBLE);
        itemCountTextView.setText(empty ? "" : String.format("Vitu %d", count));
        totalTextView.setText(String.format("TZS %s", currencyFormat.format(subtotal)));
    }
}