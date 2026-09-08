package com.example.sumayerestaurant.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.RealTimeEvent;
import com.example.sumayerestaurant.data.model.User;
import com.example.sumayerestaurant.data.websocket.WebSocketManager;
import com.example.sumayerestaurant.ui.login.LoginActivity;
import com.example.sumayerestaurant.ui.waiter.OrderHistoryActivity;
import com.example.sumayerestaurant.ui.waiter.TableListActivity;
import com.example.sumayerestaurant.util.SoundUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

public class WaiterDashboardActivity extends AppCompatActivity {
    private static final String TAG = "WaiterDashboard";

    private TokenManager tokenManager;
    private WebSocketManager webSocketManager;
    private User user;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_waiter);
        
        tokenManager = new TokenManager(this);
        webSocketManager = WebSocketManager.getInstance(this);
        user = (User) getIntent().getSerializableExtra("user");
        
        if (user == null) {
            user = tokenManager.getUser();
        }
        
        initializeUI();
        initRealTimeNotifications();
    }
    
    private void initializeUI() {
        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        TextView roleTextView = findViewById(R.id.roleTextView);
        MaterialCardView btnNewOrder = findViewById(R.id.btnNewOrder);
        MaterialCardView btnMyOrders = findViewById(R.id.btnMyOrders);
        
        String firstName = (user != null && user.getFirstName() != null) ? user.getFirstName() : "Mhudumu";
        welcomeTextView.setText("Karibu, " + firstName + "!");
        roleTextView.setText("Mhudumu");

        btnNewOrder.setOnClickListener(v -> {
            Intent intent = new Intent(WaiterDashboardActivity.this, TableListActivity.class);
            startActivity(intent);
        });

        btnMyOrders.setOnClickListener(v -> {
            Intent intent = new Intent(WaiterDashboardActivity.this, OrderHistoryActivity.class);
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
            if (id == R.id.nav_tables) {
                startActivity(new Intent(this, TableListActivity.class));
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderHistoryActivity.class));
                return true;
            }
            return true;
        });
    }

    private void initRealTimeNotifications() {
        if (user == null || user.getUsername() == null) return;

        webSocketManager.connect();
        String waiterTopic = "/topic/waiters/" + user.getUsername();

        webSocketManager.subscribe(waiterTopic, (destination, payload) -> {
            try {
                Log.d(TAG, "Waiter received real-time notification: " + payload);
                RealTimeEvent event = gson.fromJson(payload, RealTimeEvent.class);
                if (event != null && "ORDER_READY".equalsIgnoreCase(event.getEventType())) {
                    SoundUtil.playNewOrderAlert(this);
                    showOrderReadyDialog(event);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to handle waiter notification: " + e.getMessage());
            }
        });
    }

    private void showOrderReadyDialog(RealTimeEvent event) {
        new AlertDialog.Builder(this)
                .setTitle("🔔 Oda Iko Tayari!")
                .setMessage(event.getMessage() != null ? event.getMessage() : "Oda #" + event.getOrderNumber() + " iko tayari kupelekwa kwa mteja.")
                .setPositiveButton("Tazama Oda", (dialog, which) -> {
                    Intent intent = new Intent(this, OrderHistoryActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Funga", null)
                .show();
    }
    
    private void logout() {
        if (user != null && user.getUsername() != null) {
            webSocketManager.unsubscribe("/topic/waiters/" + user.getUsername());
        }
        webSocketManager.disconnect();
        tokenManager.clearAll();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
