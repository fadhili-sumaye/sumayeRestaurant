package com.example.sumayerestaurant.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.sumayerestaurant.databinding.ActivityLoginBinding;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.User;
import com.example.sumayerestaurant.ui.dashboard.DashboardActivity;
import com.example.sumayerestaurant.util.Constants;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        tokenManager = new TokenManager(this);
        
        // Check if already logged in
        if (tokenManager.isLoggedIn()) {
            goToDashboard();
            return;
        }
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        
        // Observe Login Success
        viewModel.loginSuccess.observe(this, loginResponse -> {
            if (loginResponse != null) {
                Toast.makeText(this, "Karibu " + loginResponse.getUser().getFirstName(), Toast.LENGTH_SHORT).show();
                goToDashboard();
            }
        });
        
        // Observe Error Message
        viewModel.errorMessage.observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                binding.errorMessageTextView.setVisibility(View.VISIBLE);
                binding.errorMessageTextView.setText(errorMessage);
            } else {
                binding.errorMessageTextView.setVisibility(View.GONE);
            }
        });
        
        // Observe Loading State
        viewModel.isLoading.observe(this, isLoading -> {
            binding.loginButton.setEnabled(!isLoading);
            if (isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.loginButton.setText("Inaprocessing...");
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.loginButton.setText("Ingia");
            }
        });
        
        // Click listener for login button
        binding.loginButton.setOnClickListener(v -> {
            viewModel.clearError();
            viewModel.login();
        });

        // Click listener for forgot password help
        binding.forgotPasswordTextView.setOnClickListener(v -> {
            com.example.sumayerestaurant.util.PasswordDialogHelper.showForgotPasswordHelpDialog(this);
        });
    }
    
    private void goToDashboard() {
        User user = tokenManager.getUser();
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
        finish();
    }
}
