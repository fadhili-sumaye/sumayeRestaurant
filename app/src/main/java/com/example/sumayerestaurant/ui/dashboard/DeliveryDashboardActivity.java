package com.example.sumayerestaurant.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.api.RetrofitClient;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.DeliveryOrder;
import com.example.sumayerestaurant.data.model.User;
import com.example.sumayerestaurant.data.websocket.WebSocketManager;
import com.example.sumayerestaurant.ui.adapter.DeliveryOrderAdapter;
import com.example.sumayerestaurant.ui.login.LoginActivity;
import com.example.sumayerestaurant.util.PasswordDialogHelper;
import com.example.sumayerestaurant.util.SoundUtil;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryDashboardActivity extends AppCompatActivity implements DeliveryOrderAdapter.DeliveryActionListener {

    private static final String TAG = "DeliveryDashboard";

    private TokenManager tokenManager;
    private WebSocketManager webSocketManager;
    private User user;
    private Long branchId = 1L;

    private TextView welcomeTextView;
    private TextView roleTextView;
    private TextView tvActiveDeliveries;
    private TextView tvCompletedDeliveries;
    private TextView tvEmptyState;
    private ProgressBar progressBar;
    private MaterialButton btnFilterMy;
    private MaterialButton btnFilterBranch;

    private RecyclerView recyclerViewDeliveries;
    private DeliveryOrderAdapter adapter;
    private boolean isMyDeliveriesView = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_delivery);

        tokenManager = new TokenManager(this);
        webSocketManager = WebSocketManager.getInstance(this);

        user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            user = tokenManager.getUser();
        }

        if (user != null && user.getBranchId() != null) {
            branchId = user.getBranchId();
        }

        initViews();
        setupWebSocket();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDeliveries();
    }

    private void initViews() {
        welcomeTextView = findViewById(R.id.welcomeTextView);
        roleTextView = findViewById(R.id.roleTextView);
        tvActiveDeliveries = findViewById(R.id.tvActiveDeliveries);
        tvCompletedDeliveries = findViewById(R.id.tvCompletedDeliveries);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressBar = findViewById(R.id.progressBar);
        btnFilterMy = findViewById(R.id.btnFilterMy);
        btnFilterBranch = findViewById(R.id.btnFilterBranch);

        String name = (user != null && user.getFirstName() != null) ? user.getFirstName() : "Msambazaji";
        welcomeTextView.setText("Karibu, " + name + "!");
        roleTextView.setText("Msambazaji (Delivery Rider)");

        recyclerViewDeliveries = findViewById(R.id.recyclerViewDeliveries);
        recyclerViewDeliveries.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeliveryOrderAdapter(this, this);
        recyclerViewDeliveries.setAdapter(adapter);

        btnFilterMy.setOnClickListener(v -> {
            isMyDeliveriesView = true;
            updateFilterUi();
            loadDeliveries();
        });

        btnFilterBranch.setOnClickListener(v -> {
            isMyDeliveriesView = false;
            updateFilterUi();
            loadDeliveries();
        });

        findViewById(R.id.btnRefresh).setOnClickListener(v -> loadDeliveries());

        findViewById(R.id.btnChangePassword).setOnClickListener(v -> {
            PasswordDialogHelper.showChangePasswordDialog(this);
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    private void updateFilterUi() {
        if (isMyDeliveriesView) {
            btnFilterMy.setBackgroundColor(getResources().getColor(R.color.primary));
            btnFilterMy.setTextColor(getResources().getColor(R.color.dark_blue));
            btnFilterBranch.setBackgroundColor(getResources().getColor(R.color.dark_blue_card));
            btnFilterBranch.setTextColor(getResources().getColor(R.color.secondary_text));
        } else {
            btnFilterBranch.setBackgroundColor(getResources().getColor(R.color.primary));
            btnFilterBranch.setTextColor(getResources().getColor(R.color.dark_blue));
            btnFilterMy.setBackgroundColor(getResources().getColor(R.color.dark_blue_card));
            btnFilterMy.setTextColor(getResources().getColor(R.color.secondary_text));
        }
    }

    private void setupWebSocket() {
        if (user == null || user.getUsername() == null) return;

        webSocketManager.connect();
        String riderTopic = "/topic/riders/" + user.getUsername();

        webSocketManager.subscribe(riderTopic, (destination, payload) -> {
            runOnUiThread(() -> {
                try {
                    Log.d(TAG, "Rider received push notification: " + payload);
                    SoundUtil.playNewOrderAlert(this);
                    new AlertDialog.Builder(this)
                            .setTitle("🔔 Oda Mpya ya Delivery!")
                            .setMessage("Umepewa oda mpya ya kusafirisha. Tafadhali thibitisha na anza safari.")
                            .setPositiveButton("Sawa", (d, w) -> loadDeliveries())
                            .show();
                    loadDeliveries();
                } catch (Exception e) {
                    Log.e(TAG, "Error handling rider notification: " + e.getMessage());
                }
            });
        });
    }

    private void loadDeliveries() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        Call<List<DeliveryOrder>> call;
        if (isMyDeliveriesView) {
            call = RetrofitClient.getApiService(this).getMyDeliveries(branchId);
        } else {
            call = RetrofitClient.getApiService(this).getDeliveries(branchId);
        }

        call.enqueue(new Callback<List<DeliveryOrder>>() {
            @Override
            public void onResponse(Call<List<DeliveryOrder>> call, Response<List<DeliveryOrder>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<DeliveryOrder> list = response.body();
                    adapter.setItems(list, isMyDeliveriesView);

                    int active = 0;
                    int completed = 0;
                    for (DeliveryOrder d : list) {
                        String st = d.getStatus();
                        if ("OUT_FOR_DELIVERY".equalsIgnoreCase(st) || "ASSIGNED".equalsIgnoreCase(st) || "READY".equalsIgnoreCase(st)) {
                            active++;
                        } else if ("DELIVERED".equalsIgnoreCase(st) || "COMPLETED".equalsIgnoreCase(st)) {
                            completed++;
                        }
                    }

                    tvActiveDeliveries.setText(String.valueOf(active));
                    tvCompletedDeliveries.setText(String.valueOf(completed));

                    if (list.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText(isMyDeliveriesView
                                ? "Huna oda zozote za delivery kwa sasa."
                                : "Hakuna oda za delivery za tawi zilizopo.");
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                    }
                } else {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("Imeshindwa kupakia oda za delivery (HTTP " + response.code() + ").");
                }
            }

            @Override
            public void onFailure(Call<List<DeliveryOrder>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Hitilafu ya mtandao: " + t.getMessage());
            }
        });
    }

    @Override
    public void onStatusChange(DeliveryOrder deliveryOrder, String newStatus) {
        String actionLabel = "OUT_FOR_DELIVERY".equalsIgnoreCase(newStatus) ? "kuanza safari ya delivery" : "kukamilisha na kuweka imewasilishwa";

        new AlertDialog.Builder(this)
                .setTitle("Thibitisha Hatua")
                .setMessage("Una uhakika unataka " + actionLabel + " ya oda #" + (deliveryOrder.getOrder() != null ? deliveryOrder.getOrder().getOrderNumber() : deliveryOrder.getId()) + "?")
                .setPositiveButton("Ndio, Endelea", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    RetrofitClient.getApiService(this).updateDeliveryStatus(branchId, deliveryOrder.getId(), newStatus)
                            .enqueue(new Callback<Object>() {
                                @Override
                                public void onResponse(Call<Object> call, Response<Object> response) {
                                    progressBar.setVisibility(View.GONE);
                                    if (response.isSuccessful()) {
                                        Toast.makeText(DeliveryDashboardActivity.this, "Hali ya delivery imesasishwa!", Toast.LENGTH_SHORT).show();
                                        loadDeliveries();
                                    } else {
                                        Toast.makeText(DeliveryDashboardActivity.this, "Hitilafu katika kusasisha (HTTP " + response.code() + ")", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Object> call, Throwable t) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(DeliveryDashboardActivity.this, "Hitilafu ya mtandao: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton("Ghairi", null)
                .show();
    }

    @Override
    public void onAssignToMe(DeliveryOrder deliveryOrder) {
        if (user == null || user.getId() == null) {
            Toast.makeText(this, "Mtumiaji hajatambuliwa", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Jipangie Oda Hii")
                .setMessage("Je, unataka kujipangia wewe mwenyewe kusafirisha oda #" + (deliveryOrder.getOrder() != null ? deliveryOrder.getOrder().getOrderNumber() : deliveryOrder.getId()) + "?")
                .setPositiveButton("Ndio, Jipangie", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    Map<String, Object> body = new HashMap<>();
                    body.put("riderId", user.getId());
                    RetrofitClient.getApiService(this).assignDelivery(branchId, deliveryOrder.getId(), body)
                            .enqueue(new Callback<Object>() {
                                @Override
                                public void onResponse(Call<Object> call, Response<Object> response) {
                                    progressBar.setVisibility(View.GONE);
                                    if (response.isSuccessful()) {
                                        Toast.makeText(DeliveryDashboardActivity.this, "Umejipangia oda hii kikamilifu!", Toast.LENGTH_SHORT).show();
                                        isMyDeliveriesView = true;
                                        updateFilterUi();
                                        loadDeliveries();
                                    } else {
                                        Toast.makeText(DeliveryDashboardActivity.this, "Imeshindikana kujipangia oda (HTTP " + response.code() + ")", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Object> call, Throwable t) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(DeliveryDashboardActivity.this, "Hitilafu ya mtandao: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton("Ghairi", null)
                .show();
    }

    private void logout() {
        if (user != null && user.getUsername() != null) {
            webSocketManager.unsubscribe("/topic/riders/" + user.getUsername());
        }
        webSocketManager.disconnect();
        tokenManager.clearAll();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (user != null && user.getUsername() != null) {
            webSocketManager.unsubscribe("/topic/riders/" + user.getUsername());
        }
    }
}
