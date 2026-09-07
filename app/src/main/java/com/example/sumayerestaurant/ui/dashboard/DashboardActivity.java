package com.example.sumayerestaurant.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.User;
import com.example.sumayerestaurant.util.Constants;
import com.example.sumayerestaurant.ui.login.LoginActivity;

public class DashboardActivity extends AppCompatActivity {
    private TokenManager tokenManager;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        tokenManager = new TokenManager(this);
        
        // Check if logged in
        if (!tokenManager.isLoggedIn()) {
            goToLogin();
            return;
        }
        
        // Get user from intent or from token manager
        user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            user = tokenManager.getUser();
        }
        
        if (user == null) {
            goToLogin();
            return;
        }
        
        // Route to role-specific dashboard
        routeToRoleDashboard(user);
    }
    
    private void routeToRoleDashboard(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            goToLogin();
            return;
        }
        
        Intent intent = null;
        
        // Check for roles in order of hierarchy
        if (user.getRoles().contains(Constants.ROLE_OWNER)) {
            intent = new Intent(this, OwnerDashboardActivity.class);
        } else if (user.getRoles().contains(Constants.ROLE_ADMIN)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else if (user.getRoles().contains(Constants.ROLE_MANAGER)) {
            intent = new Intent(this, ManagerDashboardActivity.class);
        } else if (user.getRoles().contains(Constants.ROLE_CASHIER)) {
            intent = new Intent(this, CashierDashboardActivity.class);
        } else if (user.getRoles().contains(Constants.ROLE_KITCHEN)) {
            intent = new Intent(this, KitchenDashboardActivity.class);
        } else if (user.getRoles().contains(Constants.ROLE_WAITER)) {
            intent = new Intent(this, WaiterDashboardActivity.class);
        } else if (user.getRoles().contains(Constants.ROLE_STOREKEEPER)) {
            intent = new Intent(this, StorekeeperDashboardActivity.class);
        } else if (user.getRoles().contains(Constants.ROLE_DELIVERY)) {
            intent = new Intent(this, DeliveryDashboardActivity.class);
        }
        
        if (intent != null) {
            intent.putExtra("user", user);
            startActivity(intent);
            finish();
        } else {
            goToLogin();
        }
    }
    
    private void goToLogin() {
        tokenManager.clearAll();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
