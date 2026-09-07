package com.example.sumayerestaurant.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.User;
import com.example.sumayerestaurant.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboardActivity extends AppCompatActivity {
    private TokenManager tokenManager;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_admin);
        
        tokenManager = new TokenManager(this);
        user = (User) getIntent().getSerializableExtra("user");
        
        if (user == null) {
            user = tokenManager.getUser();
        }
        
        initializeUI();
    }
    
    private void initializeUI() {
        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        TextView roleTextView = findViewById(R.id.roleTextView);
        Button logoutButton = findViewById(R.id.logoutButton);
        
        welcomeTextView.setText("Karibu, " + (user != null && user.getFirstName() != null ? user.getFirstName() : "Msimamizi") + "!");
        roleTextView.setText("Msimamizi (Super Admin)");

        findViewById(R.id.cardAdminCashier).setOnClickListener(v -> {
            Intent intent = new Intent(this, CashierDashboardActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });

        findViewById(R.id.cardAdminWaiter).setOnClickListener(v -> {
            Intent intent = new Intent(this, WaiterDashboardActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });

        findViewById(R.id.cardAdminKitchen).setOnClickListener(v -> {
            Intent intent = new Intent(this, KitchenDashboardActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });

        findViewById(R.id.cardAdminManager).setOnClickListener(v -> {
            Intent intent = new Intent(this, ManagerDashboardActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });

        findViewById(R.id.cardAdminReports).setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.sumayerestaurant.ui.finance.ReportActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });

        // Password Management
        findViewById(R.id.btnChangePassword).setOnClickListener(v -> com.example.sumayerestaurant.util.PasswordDialogHelper.showChangePasswordDialog(this));
        findViewById(R.id.btnResetStaffPassword).setOnClickListener(v -> com.example.sumayerestaurant.util.PasswordDialogHelper.showResetStaffPasswordDialog(this));

        logoutButton.setOnClickListener(v -> logout());
        setupBottomNav();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_reports) {
                Intent intent = new Intent(this, com.example.sumayerestaurant.ui.finance.ReportActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_cashier) {
                Intent intent = new Intent(this, CashierDashboardActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_kitchen) {
                Intent intent = new Intent(this, KitchenDashboardActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
                return true;
            }
            return true;
        });
    }

    private void logout() {
        tokenManager.clearAll();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
