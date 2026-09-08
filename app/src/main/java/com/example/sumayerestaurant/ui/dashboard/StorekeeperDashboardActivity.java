package com.example.sumayerestaurant.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.api.RetrofitClient;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.InventoryStock;
import com.example.sumayerestaurant.data.model.User;
import com.example.sumayerestaurant.ui.inventory.InventoryActivity;
import com.example.sumayerestaurant.ui.inventory.RecipePurchaseActivity;
import com.example.sumayerestaurant.ui.inventory.StockActionActivity;
import com.example.sumayerestaurant.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StorekeeperDashboardActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private User user;
    private Long branchId = 1L;

    private TextView welcomeTextView;
    private TextView roleTextView;
    private TextView tvTotalItems;
    private TextView tvLowStock;
    private TextView tvOutOfStock;
    private TextView tvStockValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_storekeeper);

        tokenManager = new TokenManager(this);
        user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            user = tokenManager.getUser();
        }

        if (user != null && user.getBranchId() != null) {
            branchId = user.getBranchId();
        }

        initViews();
        setupActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInventorySummary();
    }

    private void initViews() {
        welcomeTextView = findViewById(R.id.welcomeTextView);
        roleTextView = findViewById(R.id.roleTextView);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvLowStock = findViewById(R.id.tvLowStock);
        tvOutOfStock = findViewById(R.id.tvOutOfStock);
        tvStockValue = findViewById(R.id.tvStockValue);

        String name = (user != null && user.getFirstName() != null) ? user.getFirstName() : "Mhifadhi";
        welcomeTextView.setText("Karibu, " + name + "!");
        roleTextView.setText("Mhifadhi Ghala (Storekeeper)");
    }

    private void setupActions() {
        findViewById(R.id.btnRefresh).setOnClickListener(v -> loadInventorySummary());

        findViewById(R.id.cardStockList).setOnClickListener(v -> {
            startActivity(new Intent(this, InventoryActivity.class));
        });

        findViewById(R.id.cardAdjustStock).setOnClickListener(v -> {
            Intent intent = new Intent(this, StockActionActivity.class);
            intent.putExtra(StockActionActivity.EXTRA_MODE, "ADJUSTMENT");
            startActivity(intent);
        });

        findViewById(R.id.cardRecordWastage).setOnClickListener(v -> {
            Intent intent = new Intent(this, StockActionActivity.class);
            intent.putExtra(StockActionActivity.EXTRA_MODE, "WASTAGE");
            startActivity(intent);
        });

        findViewById(R.id.cardPurchases).setOnClickListener(v -> {
            Intent intent = new Intent(this, RecipePurchaseActivity.class);
            intent.putExtra(RecipePurchaseActivity.EXTRA_MODE, "PURCHASE");
            startActivity(intent);
        });

        findViewById(R.id.cardRecipes).setOnClickListener(v -> {
            Intent intent = new Intent(this, RecipePurchaseActivity.class);
            intent.putExtra(RecipePurchaseActivity.EXTRA_MODE, "RECIPE");
            startActivity(intent);
        });

        com.example.sumayerestaurant.util.RoleMenuUtil.attach(this,
                findViewById(R.id.btnRoleMenu), this::logout);

        setupBottomNav();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_stock) {
                startActivity(new Intent(this, InventoryActivity.class));
                return true;
            } else if (id == R.id.nav_purchases) {
                Intent intent = new Intent(this, RecipePurchaseActivity.class);
                intent.putExtra(RecipePurchaseActivity.EXTRA_MODE, "PURCHASE");
                startActivity(intent);
                return true;
            }
            return true;
        });
    }

    private void loadInventorySummary() {
        RetrofitClient.getApiService(this).getInventoryStock(branchId).enqueue(new Callback<List<InventoryStock>>() {
            @Override
            public void onResponse(Call<List<InventoryStock>> call, Response<List<InventoryStock>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<InventoryStock> items = response.body();
                    int total = items.size();
                    int low = 0;
                    int out = 0;
                    BigDecimal totalVal = BigDecimal.ZERO;

                    for (InventoryStock item : items) {
                        String badge = item.getStockBadge();
                        if ("STOCK NDOGO".equalsIgnoreCase(badge)) {
                            low++;
                        } else if ("IMEISHA".equalsIgnoreCase(badge)) {
                            out++;
                        }
                        if (item.getQuantityOnHand() != null && item.getCostPerUnit() != null) {
                            totalVal = totalVal.add(item.getQuantityOnHand().multiply(item.getCostPerUnit()));
                        }
                    }

                    tvTotalItems.setText(String.valueOf(total));
                    tvLowStock.setText(String.valueOf(low));
                    tvOutOfStock.setText(String.valueOf(out));

                    NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);
                    tvStockValue.setText("TZS " + currencyFormat.format(totalVal));
                } else {
                    Toast.makeText(StorekeeperDashboardActivity.this, "Haikuweza kupakia taarifa za ghala", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<InventoryStock>> call, Throwable t) {
                Toast.makeText(StorekeeperDashboardActivity.this, "Hitilafu ya mtandao: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        tokenManager.clearAll();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
