package com.example.sumayerestaurant.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.User;
import com.example.sumayerestaurant.ui.customer.CustomerActivity;
import com.example.sumayerestaurant.ui.delivery.DeliveryActivity;
import com.example.sumayerestaurant.ui.finance.DailyClosingActivity;
import com.example.sumayerestaurant.ui.finance.ExpenseActivity;
import com.example.sumayerestaurant.ui.finance.ReportActivity;
import com.example.sumayerestaurant.ui.inventory.InventoryDashboardActivity;
import com.example.sumayerestaurant.ui.login.LoginActivity;
import com.example.sumayerestaurant.ui.reservation.ReservationActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OwnerDashboardActivity extends AppCompatActivity {
    private TokenManager tokenManager;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_owner);
        
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
        
        String firstName = (user != null && user.getFirstName() != null) ? user.getFirstName() : "Mmiliki";
        welcomeTextView.setText("Karibu, " + firstName + "!");
        roleTextView.setText("Mmiliki wa Karamu");

        // 1. Reports & Analytics
        findViewById(R.id.cardOwnerReports).setOnClickListener(v -> startActivity(new Intent(this, ReportActivity.class)));

        // 2. Daily Closing
        findViewById(R.id.cardOwnerDailyClosing).setOnClickListener(v -> startActivity(new Intent(this, DailyClosingActivity.class)));

        // 3. Expenses
        findViewById(R.id.cardOwnerExpenses).setOnClickListener(v -> startActivity(new Intent(this, ExpenseActivity.class)));

        // 4. Inventory
        findViewById(R.id.cardOwnerInventory).setOnClickListener(v -> startActivity(new Intent(this, InventoryDashboardActivity.class)));

        // 5. Cashier & Billing
        findViewById(R.id.cardOwnerCashier).setOnClickListener(v -> {
            Intent intent = new Intent(this, CashierDashboardActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });

        // 6. Waiter & Tables
        findViewById(R.id.cardOwnerWaiter).setOnClickListener(v -> {
            Intent intent = new Intent(this, WaiterDashboardActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });

        // 7. Kitchen Display (KDS)
        findViewById(R.id.cardOwnerKitchen).setOnClickListener(v -> {
            Intent intent = new Intent(this, KitchenDashboardActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });

        // 8. Customers
        findViewById(R.id.cardOwnerCustomers).setOnClickListener(v -> startActivity(new Intent(this, CustomerActivity.class)));

        // 9. Reservations
        findViewById(R.id.cardOwnerReservations).setOnClickListener(v -> startActivity(new Intent(this, ReservationActivity.class)));

        // 10. Delivery
        findViewById(R.id.cardOwnerDelivery).setOnClickListener(v -> startActivity(new Intent(this, DeliveryActivity.class)));

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
                startActivity(new Intent(this, ReportActivity.class));
                return true;
            } else if (id == R.id.nav_expenses) {
                startActivity(new Intent(this, ExpenseActivity.class));
                return true;
            } else if (id == R.id.nav_inventory) {
                startActivity(new Intent(this, InventoryDashboardActivity.class));
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
