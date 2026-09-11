package com.example.sumayerestaurant.ui.waiter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.local.CartManager;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.RestaurantTable;
import com.example.sumayerestaurant.data.model.User;
import com.example.sumayerestaurant.ui.adapter.TableListAdapter;
import com.example.sumayerestaurant.ui.viewmodel.TableViewModel;
import com.google.android.material.appbar.MaterialToolbar;

public class TableListActivity extends AppCompatActivity {

    private TableViewModel tableViewModel;
    private TableListAdapter adapter;
    private TokenManager tokenManager;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private Long branchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_list);

        tokenManager = new TokenManager(this);
        User user = tokenManager.getUser();
        if (user != null && user.getBranchId() != null) {
            branchId = user.getBranchId();
        } else {
            Toast.makeText(this, "Akaunti hii haina tawi lililowekwa.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupViewModel();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewTables);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new TableListAdapter(this, table -> {
            CartManager.getInstance().setSelectedTable(table);
            Intent intent = new Intent(TableListActivity.this, MenuActivity.class);
            intent.putExtra("table", table);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        tableViewModel = new ViewModelProvider(this).get(TableViewModel.class);

        tableViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        tableViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        tableViewModel.getTables().observe(this, tables -> {
            adapter.setTables(tables);
            tvEmptyState.setVisibility(tables == null || tables.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        tableViewModel.loadTables(branchId);
    }
}
