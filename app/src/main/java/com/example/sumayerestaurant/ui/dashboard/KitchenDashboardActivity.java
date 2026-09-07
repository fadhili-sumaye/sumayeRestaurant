package com.example.sumayerestaurant.ui.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.KitchenOrder;
import com.example.sumayerestaurant.data.model.User;
import com.example.sumayerestaurant.data.websocket.StompClient;
import com.example.sumayerestaurant.ui.adapter.KitchenOrderAdapter;
import com.example.sumayerestaurant.ui.login.LoginActivity;
import com.example.sumayerestaurant.ui.viewmodel.KitchenViewModel;
import com.example.sumayerestaurant.util.SoundUtil;

public class KitchenDashboardActivity extends AppCompatActivity {

    private static final String PREF_KITCHEN_SOUND = "kitchen_sound_enabled";

    private KitchenViewModel viewModel;
    private KitchenOrderAdapter adapter;
    private TokenManager tokenManager;
    private SharedPreferences sharedPreferences;

    private View layoutConnectionStatus;
    private TextView tvConnectionStatus;
    private ImageButton btnSoundToggle;
    private TextView tvActiveOrdersCount;
    private TextView btnToggleSort;
    private ProgressBar progressBar;
    private TextView tvEmptyKitchen;

    private TextView chipAll;
    private TextView chipNew;
    private TextView chipPreparing;
    private TextView chipReady;

    private boolean isSoundEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_kitchen);

        tokenManager = new TokenManager(this);
        sharedPreferences = getSharedPreferences("kitchen_prefs", MODE_PRIVATE);
        isSoundEnabled = sharedPreferences.getBoolean(PREF_KITCHEN_SOUND, true);

        initViews();
        setupViewModel();
    }

    private void initViews() {
        layoutConnectionStatus = findViewById(R.id.layoutConnectionStatus);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        btnSoundToggle = findViewById(R.id.btnSoundToggle);
        tvActiveOrdersCount = findViewById(R.id.tvActiveOrdersCount);
        btnToggleSort = findViewById(R.id.btnToggleSort);
        progressBar = findViewById(R.id.progressBarKitchen);
        tvEmptyKitchen = findViewById(R.id.tvEmptyKitchen);

        chipAll = findViewById(R.id.chipFilterAll);
        chipNew = findViewById(R.id.chipFilterNew);
        chipPreparing = findViewById(R.id.chipFilterPreparing);
        chipReady = findViewById(R.id.chipFilterReady);

        updateSoundToggleUI();
        btnSoundToggle.setOnClickListener(v -> {
            isSoundEnabled = !isSoundEnabled;
            sharedPreferences.edit().putBoolean(PREF_KITCHEN_SOUND, isSoundEnabled).apply();
            updateSoundToggleUI();
            Toast.makeText(this, isSoundEnabled ? "Sauti ya oda imewashwa" : "Sauti ya oda imezimwa", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnPasswordKitchen).setOnClickListener(v -> com.example.sumayerestaurant.util.PasswordDialogHelper.showChangePasswordDialog(this));
        findViewById(R.id.btnLogoutKitchen).setOnClickListener(v -> logout());

        setupFilterChips();

        btnToggleSort.setOnClickListener(v -> {
            boolean current = viewModel.isOldestFirst();
            viewModel.setSortOldestFirst(!current);
            btnToggleSort.setText(!current ? "⏱️ Ya Zamani Kwanza" : "⏱️ Mpya Kwanza");
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerViewKitchenOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new KitchenOrderAdapter(this, new KitchenOrderAdapter.OnKitchenActionListener() {
            @Override
            public void onAccept(KitchenOrder order) {
                viewModel.acceptOrder(order.getId());
            }

            @Override
            public void onStartPreparing(KitchenOrder order) {
                viewModel.startPreparingOrder(order.getId());
            }

            @Override
            public void onMarkReady(KitchenOrder order) {
                viewModel.markOrderReady(order.getId());
            }

            @Override
            public void onCancel(KitchenOrder order) {
                showCancelDialog(order);
            }

            @Override
            public void onOrderClick(KitchenOrder order) {
                // Future detail expansion if needed
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupFilterChips() {
        chipAll.setOnClickListener(v -> {
            highlightChip(chipAll);
            viewModel.setFilter("ALL");
        });

        chipNew.setOnClickListener(v -> {
            highlightChip(chipNew);
            viewModel.setFilter("NEW");
        });

        chipPreparing.setOnClickListener(v -> {
            highlightChip(chipPreparing);
            viewModel.setFilter("PREPARING");
        });

        chipReady.setOnClickListener(v -> {
            highlightChip(chipReady);
            viewModel.setFilter("READY");
        });
    }

    private void highlightChip(TextView selected) {
        TextView[] chips = new TextView[]{chipAll, chipNew, chipPreparing, chipReady};
        for (TextView chip : chips) {
            if (chip == selected) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected);
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_color));
            }
        }
    }

    private void updateSoundToggleUI() {
        if (isSoundEnabled) {
            btnSoundToggle.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
            btnSoundToggle.setColorFilter(ContextCompat.getColor(this, R.color.brand_green));
        } else {
            btnSoundToggle.setImageResource(android.R.drawable.ic_lock_silent_mode);
            btnSoundToggle.setColorFilter(ContextCompat.getColor(this, R.color.secondary_text));
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(KitchenViewModel.class);

        // Connection State
        viewModel.getConnectionState().observe(this, state -> {
            if (state == StompClient.ConnectionState.CONNECTED) {
                layoutConnectionStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_success_background));
                tvConnectionStatus.setText("🟢 Mtandao umeunganishwa");
                tvConnectionStatus.setTextColor(ContextCompat.getColor(this, R.color.success_color));
            } else if (state == StompClient.ConnectionState.CONNECTING) {
                layoutConnectionStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_warning_background));
                tvConnectionStatus.setText("🟡 Inajaribu kuunganishwa tena...");
                tvConnectionStatus.setTextColor(ContextCompat.getColor(this, R.color.warning_color));
            } else {
                layoutConnectionStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_error_background));
                tvConnectionStatus.setText("🔴 Muunganisho umepotea");
                tvConnectionStatus.setTextColor(ContextCompat.getColor(this, R.color.error_color));
            }
        });

        // Loading indicator
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Error message
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        // Filtered Orders list
        viewModel.getFilteredOrders().observe(this, orders -> {
            adapter.setOrders(orders);
            int count = orders != null ? orders.size() : 0;
            tvActiveOrdersCount.setText("Oda Zinazoendelea: " + count);
            tvEmptyKitchen.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        });

        // Real-time Event trigger
        viewModel.getNewEvent().observe(this, event -> {
            if (event != null && "NEW_KOT".equalsIgnoreCase(event.getEventType())) {
                if (isSoundEnabled) {
                    SoundUtil.playNewOrderAlert(this);
                }
                Toast.makeText(this, "🔔 " + event.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.initRealTime();
    }

    private void showCancelDialog(KitchenOrder order) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_cancel_order, null);
        EditText etReason = dialogView.findViewById(R.id.etCancelReason);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Ghairi Oda", (dialog, which) -> {
                    String reason = etReason.getText().toString().trim();
                    if (reason.isEmpty()) {
                        reason = "Imeghairiwa na jikoni";
                    }
                    viewModel.cancelOrder(order.getId(), reason);
                })
                .setNegativeButton("Rudi", null)
                .show();
    }

    private void logout() {
        tokenManager.clearAll();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
