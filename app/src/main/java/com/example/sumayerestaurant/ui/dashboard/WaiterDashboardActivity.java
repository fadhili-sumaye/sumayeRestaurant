package com.example.sumayerestaurant.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
        Button logoutButton = findViewById(R.id.logoutButton);
        
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

        findViewById(R.id.btnChangePassword).setOnClickListener(v -> com.example.sumayerestaurant.util.PasswordDialogHelper.showChangePasswordDialog(this));
        
        logoutButton.setOnClickListener(v -> logout());
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
