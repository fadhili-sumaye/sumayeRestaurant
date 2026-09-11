package com.example.sumayerestaurant.ui.waiter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.MenuItem;
import com.example.sumayerestaurant.data.model.RestaurantTable;
import com.example.sumayerestaurant.data.model.User;
import com.example.sumayerestaurant.ui.adapter.CategoryChipAdapter;
import com.example.sumayerestaurant.ui.adapter.MenuItemAdapter;
import com.example.sumayerestaurant.ui.viewmodel.MenuViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.Locale;

public class MenuActivity extends AppCompatActivity {

    private MenuViewModel menuViewModel;
    private CategoryChipAdapter categoryAdapter;
    private MenuItemAdapter menuAdapter;
    private TokenManager tokenManager;

    private ProgressBar progressBar;
    private TextView tvEmptyMenu;
    private MaterialCardView layoutCartBottomBar;
    private TextView tvCartBottomCount;
    private TextView tvCartBottomTotal;
    private Long branchId;
    private RestaurantTable table;
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        tokenManager = new TokenManager(this);
        User user = tokenManager.getUser();
        if (user != null && user.getBranchId() != null) {
            branchId = user.getBranchId();
        } else {
            Toast.makeText(this, "Akaunti hii haina tawi lililowekwa.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        table = (RestaurantTable) getIntent().getSerializableExtra("table");
        if (table == null) {
            table = CartManager.getInstance().getSelectedTable();
        }

        initViews();
        setupViewModel();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvTableBanner = findViewById(R.id.tvSelectedTableBanner);
        if (table != null) {
            tvTableBanner.setText("Meza " + table.getTableNumber() + " (" + table.getStatusLabelSwahili() + ")");
        } else {
            tvTableBanner.setText("Bila Meza");
        }

        progressBar = findViewById(R.id.progressBar);
        tvEmptyMenu = findViewById(R.id.tvEmptyMenu);

        // Search Input
        EditText etSearchFood = findViewById(R.id.etSearchFood);
        etSearchFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                menuViewModel.search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Categories RecyclerView
        RecyclerView recyclerCategories = findViewById(R.id.recyclerViewCategories);
        recyclerCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryChipAdapter(this, categoryId -> {
            menuViewModel.filterByCategory(categoryId);
        });
        recyclerCategories.setAdapter(categoryAdapter);

        // Menu Items RecyclerView
        RecyclerView recyclerMenuItems = findViewById(R.id.recyclerViewMenuItems);
        recyclerMenuItems.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter = new MenuItemAdapter(this, (item, currentInstructions) -> {
            showSpecialInstructionsDialog(item, currentInstructions);
        });
        recyclerMenuItems.setAdapter(menuAdapter);

        // Bottom Cart Sticky Bar
        layoutCartBottomBar = findViewById(R.id.layoutCartBottomBar);
        tvCartBottomCount = findViewById(R.id.tvCartBottomCount);
        tvCartBottomTotal = findViewById(R.id.tvCartBottomTotal);

        findViewById(R.id.btnOpenCart).setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, CartActivity.class));
        });
    }

    private void setupViewModel() {
        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);

        menuViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        menuViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        menuViewModel.getCategories().observe(this, categories -> {
            categoryAdapter.setCategories(categories);
        });

        menuViewModel.getFilteredMenuItems().observe(this, items -> {
            menuAdapter.setItems(items);
            tvEmptyMenu.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Observe Cart Count & Total for Bottom Bar
        menuViewModel.getCartItemCount().observe(this, count -> {
            if (count != null && count > 0) {
                layoutCartBottomBar.setVisibility(View.VISIBLE);
                tvCartBottomCount.setText("Vyakula " + count);
            } else {
                layoutCartBottomBar.setVisibility(View.GONE);
            }
        });

        menuViewModel.getCartSubtotal().observe(this, subtotal -> {
            if (subtotal != null) {
                tvCartBottomTotal.setText(String.format("TZS %s", currencyFormat.format(subtotal)));
            }
        });

        menuViewModel.loadMenu(branchId);
    }

    private void showSpecialInstructionsDialog(MenuItem item, String currentInstructions) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_special_instructions, null);
        TextView tvSubtitle = dialogView.findViewById(R.id.dialogItemSubtitle);
        EditText etInstructions = dialogView.findViewById(R.id.etSpecialInstructions);

        tvSubtitle.setText(item.getName());
        if (currentInstructions != null) {
            etInstructions.setText(currentInstructions);
            etInstructions.setSelection(currentInstructions.length());
        }

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Hifadhi", (dialog, which) -> {
                    String note = etInstructions.getText().toString().trim();
                    // If item not yet in cart, add 1 with note, else update note
                    int currentQty = menuViewModel.getItemQuantityInCart(item.getId());
                    if (currentQty == 0) {
                        menuViewModel.addItemToCart(item, 1, note);
                    } else {
                        CartManager.getInstance().updateSpecialInstructions(item.getId(), note);
                    }
                    menuAdapter.notifyDataSetChanged();
                })
                .setNegativeButton("Ghairi", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (menuAdapter != null) {
            menuAdapter.notifyDataSetChanged();
        }
    }
}
