package com.example.sumayerestaurant.ui.inventory;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sumayerestaurant.data.api.RetrofitClient;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.InventoryStock;
import com.example.sumayerestaurant.data.model.User;
import com.example.sumayerestaurant.util.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Simple, accessible branch-stock list; managers never enter stock quantities here. */
public class InventoryActivity extends AppCompatActivity {
    private LinearLayout stockList;
    private ProgressBar loading;
    private TextView emptyState;
    private final List<InventoryStock> stocks = new ArrayList<>();

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        ThemeUtil.applyRootTheme(root);
        TextView title = new TextView(this);
        title.setText("Hisa za tawi"); ThemeUtil.styleTitle(title, 24);
        root.addView(title);
        EditText search = new EditText(this);
        search.setHint("Tafuta kiungo, mfano mchele"); ThemeUtil.styleEditText(search);
        root.addView(search);
        loading = new ProgressBar(this); root.addView(loading);
        emptyState = new TextView(this);
        emptyState.setText("Hakuna bidhaa zilizopatikana."); ThemeUtil.styleLabel(emptyState, 16); emptyState.setTextColor(ThemeUtil.textSecondary(this));
        emptyState.setVisibility(View.GONE); root.addView(emptyState);
        stockList = new LinearLayout(this); stockList.setOrientation(LinearLayout.VERTICAL); root.addView(stockList);
        setContentView(root);
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) { showStocks(s.toString()); }
            public void afterTextChanged(Editable e) { }
        });
        loadStock();
    }

    private void loadStock() {
        User user = new TokenManager(this).getUser();
        Long branchId = (user != null && user.getBranchId() != null) ? user.getBranchId() : 1L;
        loading.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(this).getInventoryStock(branchId).enqueue(new Callback<List<InventoryStock>>() {
            public void onResponse(Call<List<InventoryStock>> call, Response<List<InventoryStock>> response) {
                loading.setVisibility(View.GONE);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(InventoryActivity.this, "Imeshindikana kupakia hisa. Tafadhali jaribu tena.", Toast.LENGTH_LONG).show(); return;
                }
                stocks.clear(); stocks.addAll(response.body()); showStocks("");
            }
            public void onFailure(Call<List<InventoryStock>> call, Throwable t) {
                loading.setVisibility(View.GONE);
                Toast.makeText(InventoryActivity.this, "Hakuna mtandao. Tafadhali jaribu tena.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showStocks(String query) {
        stockList.removeAllViews();
        String q = query.trim().toLowerCase(); int visible = 0;
        for (InventoryStock stock : stocks) {
            if (!stock.getIngredientName().toLowerCase().contains(q)) continue;
            TextView item = new TextView(this);
            String badge = stock.getStockBadge() == null ? "IPO" : stock.getStockBadge();
            item.setText(stock.getIngredientName() + "\n" + stock.getQuantityOnHand() + " " + stock.getUnit()
                    + "  •  Kiwango cha chini: " + stock.getMinimumStockLevel() + "\n" + badge);
            item.setTextSize(17); item.setMinHeight(128); item.setPadding(20, 20, 20, 20);
            item.setTextColor("STOCK NDOGO".equals(badge) || "IMEISHA".equals(badge) ? ThemeUtil.warning(this) : ThemeUtil.textPrimary(this));
            stockList.addView(item); visible++;
        }
        emptyState.setVisibility(visible == 0 ? View.VISIBLE : View.GONE);
    }
}
