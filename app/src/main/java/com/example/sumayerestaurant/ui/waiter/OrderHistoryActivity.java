package com.example.sumayerestaurant.ui.waiter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.Order;
import com.example.sumayerestaurant.data.model.User;
import com.example.sumayerestaurant.data.websocket.WebSocketManager;
import com.example.sumayerestaurant.ui.adapter.OrderHistoryAdapter;
import com.example.sumayerestaurant.ui.viewmodel.OrderHistoryViewModel;
import com.google.android.material.appbar.MaterialToolbar;

public class OrderHistoryActivity extends AppCompatActivity {

    private OrderHistoryViewModel viewModel;
    private OrderHistoryAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyOrders;
    private WebSocketManager webSocketManager;
    private TokenManager tokenManager;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        tokenManager = new TokenManager(this);
        webSocketManager = WebSocketManager.getInstance(this);
        user = tokenManager.getUser();

        initViews();
        setupViewModel();
        setupRealTime();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progressBar);
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderHistoryAdapter(this, this::openOrderDetail);
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(OrderHistoryViewModel.class);

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getOrders().observe(this, orders -> {
            adapter.setOrders(orders);
            tvEmptyOrders.setVisibility(orders == null || orders.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void setupRealTime() {
        if (user == null || user.getUsername() == null) return;
        webSocketManager.connect();
        String topic = "/topic/waiters/" + user.getUsername();
        webSocketManager.subscribe(topic, (destination, payload) -> {
            // Live refresh list upon order status update
            viewModel.loadMyOrders();
        });
    }

    private void openOrderDetail(Order order) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("order", order);
        intent.putExtra("orderId", order.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadMyOrders();
    }
}
