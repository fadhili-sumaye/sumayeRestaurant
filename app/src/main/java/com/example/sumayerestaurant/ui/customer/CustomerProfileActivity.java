package com.example.sumayerestaurant.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.ui.dashboard.DashboardActivity;
import com.example.sumayerestaurant.ui.login.LoginActivity;

/**
 * Customer profile screen with a link to the staff login/dashboard.
 */
public class CustomerProfileActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        tokenManager = new TokenManager(this);
        statusTextView = findViewById(R.id.statusTextView);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        findViewById(R.id.staffLoginButton).setOnClickListener(v -> {
            if (tokenManager.isLoggedIn()) {
                startActivity(new Intent(this, DashboardActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tokenManager.isLoggedIn()) {
            statusTextView.setText("Umeingia kama mfanyakazi. Bonyeza kufungua dashboard yako.");
        } else {
            statusTextView.setText("Hujaingia. Ingia kama mfanyakazi kuendesha oda, meza, hisa na ripoti.");
        }
    }
}