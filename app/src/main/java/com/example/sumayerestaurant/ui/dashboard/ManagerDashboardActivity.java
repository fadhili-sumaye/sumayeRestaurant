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
import com.example.sumayerestaurant.util.PasswordDialogHelper;

public class ManagerDashboardActivity extends AppCompatActivity {
    private TokenManager tokenManager;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_manager);
        
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
        
        String firstName = (user != null && user.getFirstName() != null) ? user.getFirstName() : "Meneja";
        welcomeTextView.setText("Karibu, " + firstName + "!");
        roleTextView.setText("Meneja");

        Button inventoryButton = findViewById(R.id.inventoryButton);
        inventoryButton.setOnClickListener(v -> startActivity(new Intent(this, InventoryDashboardActivity.class)));
        findViewById(R.id.customersButton).setOnClickListener(v -> startActivity(new Intent(this, CustomerActivity.class)));
        findViewById(R.id.reservationsButton).setOnClickListener(v -> startActivity(new Intent(this, ReservationActivity.class)));
        findViewById(R.id.deliveryButton).setOnClickListener(v -> startActivity(new Intent(this, DeliveryActivity.class)));
        findViewById(R.id.expenseButton).setOnClickListener(v -> startActivity(new Intent(this, ExpenseActivity.class)));
        findViewById(R.id.dailyClosingButton).setOnClickListener(v -> startActivity(new Intent(this, DailyClosingActivity.class)));
        findViewById(R.id.reportsButton).setOnClickListener(v -> startActivity(new Intent(this, ReportActivity.class)));
        
        // Password Management
        findViewById(R.id.btnChangePassword).setOnClickListener(v -> PasswordDialogHelper.showChangePasswordDialog(this));
        findViewById(R.id.btnResetStaffPassword).setOnClickListener(v -> PasswordDialogHelper.showResetStaffPasswordDialog(this));

        logoutButton.setOnClickListener(v -> logout());
    }
    
    private void logout() {
        tokenManager.clearAll();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
