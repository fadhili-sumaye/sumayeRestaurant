package com.example.sumayerestaurant.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import com.example.sumayerestaurant.data.model.Bill;
import com.example.sumayerestaurant.data.model.User;
import com.example.sumayerestaurant.data.websocket.StompClient;
import com.example.sumayerestaurant.ui.adapter.BillAdapter;
import com.example.sumayerestaurant.ui.cashier.PaymentActivity;
import com.example.sumayerestaurant.ui.cashier.ReceiptActivity;
import com.example.sumayerestaurant.ui.login.LoginActivity;
import com.example.sumayerestaurant.ui.viewmodel.CashierViewModel;
import com.example.sumayerestaurant.util.SoundUtil;

import java.math.BigDecimal;
import java.util.Locale;

public class CashierDashboardActivity extends AppCompatActivity {

    private CashierViewModel viewModel;
    private BillAdapter adapter;
    private TokenManager tokenManager;
    private User user;

    private View layoutConnectionStatus;
    private TextView tvConnectionStatus;
    private TextView tvTodaySales;
    private TextView tvUnpaidBillsCount;
    private TextView tvPaidBillsCount;
    private EditText etSearch;
    private ProgressBar progressBar;
    private TextView tvEmptyBills;

    private TextView chipUnpaid;
    private TextView chipPaid;
    private TextView chipAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_cashier);

        tokenManager = new TokenManager(this);
        user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            user = tokenManager.getUser();
        }

        initViews();
        setupViewModel();
    }

    private void initViews() {
        layoutConnectionStatus = findViewById(R.id.layoutCashierConnectionStatus);
        tvConnectionStatus = findViewById(R.id.tvCashierConnectionStatus);
        tvTodaySales = findViewById(R.id.tvTodaySales);
        tvUnpaidBillsCount = findViewById(R.id.tvUnpaidBillsCount);
        tvPaidBillsCount = findViewById(R.id.tvPaidBillsCount);
        etSearch = findViewById(R.id.etSearchBills);
        progressBar = findViewById(R.id.progressBarCashier);
        tvEmptyBills = findViewById(R.id.tvEmptyBills);

        chipUnpaid = findViewById(R.id.chipFilterUnpaid);
        chipPaid = findViewById(R.id.chipFilterPaid);
        chipAll = findViewById(R.id.chipFilterAllBills);

        findViewById(R.id.btnRefreshCashier).setOnClickListener(v -> viewModel.loadBills());
        findViewById(R.id.btnPasswordCashier).setOnClickListener(v -> com.example.sumayerestaurant.util.PasswordDialogHelper.showChangePasswordDialog(this));
        findViewById(R.id.btnLogoutCashier).setOnClickListener(v -> logout());

        setupFilterChips();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int count, int after) {
                viewModel.setSearchQuery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerViewBills);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BillAdapter(this, new BillAdapter.OnBillActionListener() {
            @Override
            public void onProcessPayment(Bill bill) {
                Intent intent = new Intent(CashierDashboardActivity.this, PaymentActivity.class);
                intent.putExtra("bill", bill);
                intent.putExtra("billId", bill.getId());
                startActivity(intent);
            }

            @Override
            public void onViewReceipt(Bill bill) {
                Intent intent = new Intent(CashierDashboardActivity.this, ReceiptActivity.class);
                intent.putExtra("billId", bill.getId());
                startActivity(intent);
            }

            @Override
            public void onBillClick(Bill bill) {
                if (!"PAID".equalsIgnoreCase(bill.getPaymentStatus())) {
                    onProcessPayment(bill);
                } else {
                    onViewReceipt(bill);
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupFilterChips() {
        chipUnpaid.setOnClickListener(v -> {
            highlightChip(chipUnpaid);
            viewModel.setFilter("UNPAID");
        });

        chipPaid.setOnClickListener(v -> {
            highlightChip(chipPaid);
            viewModel.setFilter("PAID");
        });

        chipAll.setOnClickListener(v -> {
            highlightChip(chipAll);
            viewModel.setFilter("ALL");
        });
    }

    private void highlightChip(TextView selected) {
        TextView[] chips = new TextView[]{chipUnpaid, chipPaid, chipAll};
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

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CashierViewModel.class);

        // Connection status
        viewModel.getConnectionState().observe(this, state -> {
            if (state == StompClient.ConnectionState.CONNECTED) {
                layoutConnectionStatus.setBackgroundColor(0xFFE8F5E9);
                tvConnectionStatus.setText("🟢 Mtandao umeunganishwa");
                tvConnectionStatus.setTextColor(0xFF2E7D32);
            } else if (state == StompClient.ConnectionState.CONNECTING) {
                layoutConnectionStatus.setBackgroundColor(0xFFFFF3E0);
                tvConnectionStatus.setText("🟡 Inajaribu kuunganishwa tena...");
                tvConnectionStatus.setTextColor(0xFFE65100);
            } else {
                layoutConnectionStatus.setBackgroundColor(0xFFFFEBEE);
                tvConnectionStatus.setText("🔴 Muunganisho umepotea");
                tvConnectionStatus.setTextColor(0xFFC62828);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getTotalSalesToday().observe(this, totalSales -> {
            tvTodaySales.setText("TZS " + String.format(Locale.getDefault(), "%,.0f", totalSales.doubleValue()));
        });

        viewModel.getUnpaidCount().observe(this, count -> {
            tvUnpaidBillsCount.setText(String.valueOf(count));
        });

        viewModel.getPaidCount().observe(this, count -> {
            tvPaidBillsCount.setText(String.valueOf(count));
        });

        viewModel.getFilteredBills().observe(this, bills -> {
            adapter.setBills(bills);
            tvEmptyBills.setVisibility(bills == null || bills.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Real-time Bill Requested Alert
        viewModel.getBillRequestedEvent().observe(this, event -> {
            if (event != null) {
                SoundUtil.playNewOrderAlert(this);
                new AlertDialog.Builder(this)
                        .setTitle("🔔 Ombi la Ankara!")
                        .setMessage(event.getMessage())
                        .setPositiveButton("Tazama Ankara", (dialog, which) -> {
                            viewModel.setFilter("UNPAID");
                            highlightChip(chipUnpaid);
                        })
                        .setNegativeButton("Funga", null)
                        .show();
            }
        });

        viewModel.initRealTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadBills();
    }

    private void logout() {
        tokenManager.clearAll();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
