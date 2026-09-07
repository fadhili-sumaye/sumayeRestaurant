package com.example.sumayerestaurant.ui.waiter;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import com.example.sumayerestaurant.data.model.Bill;
import com.example.sumayerestaurant.data.model.Order;
import com.example.sumayerestaurant.data.repository.BillingRepository;
import com.example.sumayerestaurant.ui.adapter.OrderDetailItemAdapter;
import com.example.sumayerestaurant.ui.viewmodel.OrderHistoryViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private OrderHistoryViewModel viewModel;
    private BillingRepository billingRepository;
    private OrderDetailItemAdapter adapter;

    private TextView tvOrderNumber;
    private TextView tvStatus;
    private TextView tvTable;
    private TextView tvDate;
    private TextView tvWaiter;
    private TextView tvNotes;
    private View layoutCancellationInfo;
    private TextView tvCancellationReason;
    private TextView tvCancelledBy;
    private TextView tvSubtotal;
    private TextView tvTotal;
    private MaterialButton btnRequestBill;
    private MaterialButton btnCancelOrder;
    private ProgressBar progressBar;

    private Long orderId;
    private Order currentOrder;
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        billingRepository = new BillingRepository(this);
        currentOrder = (Order) getIntent().getSerializableExtra("order");
        orderId = getIntent().getLongExtra("orderId", -1);
        if (orderId == -1 && currentOrder != null) {
            orderId = currentOrder.getId();
        }

        initViews();
        setupViewModel();

        if (currentOrder != null) {
            displayOrder(currentOrder);
        }

        if (orderId != null && orderId != -1) {
            viewModel.loadOrderDetail(orderId);
        }
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvOrderNumber = findViewById(R.id.tvDetailOrderNumber);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvTable = findViewById(R.id.tvDetailTable);
        tvDate = findViewById(R.id.tvDetailDate);
        tvWaiter = findViewById(R.id.tvDetailWaiter);
        tvNotes = findViewById(R.id.tvDetailNotes);
        layoutCancellationInfo = findViewById(R.id.layoutCancellationInfo);
        tvCancellationReason = findViewById(R.id.tvCancellationReason);
        tvCancelledBy = findViewById(R.id.tvCancelledBy);
        tvSubtotal = findViewById(R.id.tvDetailSubtotal);
        tvTotal = findViewById(R.id.tvDetailTotal);
        btnRequestBill = findViewById(R.id.btnRequestBill);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        progressBar = findViewById(R.id.progressBar);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewOrderItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderDetailItemAdapter(this);
        recyclerView.setAdapter(adapter);

        btnRequestBill.setOnClickListener(v -> requestBill());
        btnCancelOrder.setOnClickListener(v -> showCancelConfirmationDialog());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(OrderHistoryViewModel.class);

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getOrderDetail().observe(this, order -> {
            if (order != null) {
                currentOrder = order;
                displayOrder(order);
            }
        });

        viewModel.getCancelSuccess().observe(this, cancelledOrder -> {
            if (cancelledOrder != null) {
                Toast.makeText(this, "Oda imeghairiwa kwa mafanikio.", Toast.LENGTH_LONG).show();
                currentOrder = cancelledOrder;
                displayOrder(cancelledOrder);
            }
        });
    }

    private void displayOrder(Order order) {
        tvOrderNumber.setText(order.getOrderNumber());
        tvStatus.setText(order.getStatusLabelSwahili());

        if (order.getTableNumber() != null) {
            tvTable.setText(String.format("🍽️ Meza: %d (%s)", order.getTableNumber(), order.getOrderType()));
        } else {
            tvTable.setText(String.format("📦 Aina ya Oda: %s", order.getOrderType()));
        }

        String dateStr = order.getCreatedAt() != null ? order.getCreatedAt().replace("T", " ") : "";
        tvDate.setText("🕒 Tarehe: " + dateStr);

        if (order.getWaiterName() != null) {
            tvWaiter.setText("👤 Mhudumu: " + order.getWaiterName());
        }

        if (!TextUtils.isEmpty(order.getNotes())) {
            tvNotes.setText("Dokezo: " + order.getNotes());
            tvNotes.setVisibility(View.VISIBLE);
        } else {
            tvNotes.setVisibility(View.GONE);
        }

        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "";
        if ("CANCELLED".equals(status)) {
            tvStatus.setBackgroundResource(R.drawable.bg_badge_occupied);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_occupied));
            layoutCancellationInfo.setVisibility(View.VISIBLE);
            tvCancellationReason.setText("Sababu ya kughairi: " + (order.getCancellationReason() != null ? order.getCancellationReason() : "-"));
            tvCancelledBy.setText("Imeghairiwa na: " + (order.getCancelledByName() != null ? order.getCancelledByName() : "Mhudumu"));
            btnCancelOrder.setVisibility(View.GONE);
            btnRequestBill.setVisibility(View.GONE);
        } else {
            layoutCancellationInfo.setVisibility(View.GONE);
            if ("COMPLETED".equals(status)) {
                tvStatus.setBackgroundResource(R.drawable.bg_badge_available);
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_available));
                btnCancelOrder.setVisibility(View.GONE);
                btnRequestBill.setVisibility(View.GONE);
            } else {
                tvStatus.setBackgroundResource(R.drawable.bg_badge_reserved);
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.primary));
                btnCancelOrder.setVisibility(View.VISIBLE);
                btnRequestBill.setVisibility(View.VISIBLE);
            }
        }

        tvSubtotal.setText(String.format("TZS %s", currencyFormat.format(order.getSubtotal())));
        tvTotal.setText(String.format("TZS %s", currencyFormat.format(order.getTotalAmount())));

        if (order.getItems() != null) {
            adapter.setItems(order.getItems());
        }
    }

    private void requestBill() {
        if (orderId == null || orderId == -1) return;
        btnRequestBill.setEnabled(false);
        btnRequestBill.setText("Inatuma Ombi...");

        billingRepository.requestBill(orderId, new BillingRepository.BillingCallback<>() {
            @Override
            public void onSuccess(Bill result) {
                btnRequestBill.setEnabled(true);
                btnRequestBill.setText("✅ Ombi Limetumwa");
                Toast.makeText(OrderDetailActivity.this, "Ombi la Ankara limetumwa kwa mweka hazina!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String message) {
                btnRequestBill.setEnabled(true);
                btnRequestBill.setText("📄 Omba Ankara (Bill)");
                Toast.makeText(OrderDetailActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showCancelConfirmationDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_cancel_order, null);
        EditText etReason = dialogView.findViewById(R.id.etCancelReason);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Ghairi Oda Hii", (dialog, which) -> {
                    String reason = etReason.getText().toString().trim();
                    if (TextUtils.isEmpty(reason)) {
                        reason = "Imeghairiwa na mhudumu";
                    }
                    if (orderId != null && orderId != -1) {
                        viewModel.cancelOrder(orderId, reason);
                    }
                })
                .setNegativeButton("Rudi", null)
                .show();
    }
}
