package com.example.sumayerestaurant.ui.cashier;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.model.Receipt;
import com.example.sumayerestaurant.data.repository.BillingRepository;
import com.example.sumayerestaurant.ui.adapter.ReceiptItemAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.math.BigDecimal;
import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {

    private BillingRepository billingRepository;
    private Long billId;

    private TextView tvRestaurantName;
    private TextView tvBranchInfo;
    private TextView tvTaxInfo;
    private TextView tvReceiptNumber;
    private TextView tvReceiptDate;
    private TextView tvOrderTable;
    private TextView tvCashier;

    private RecyclerView recyclerItems;
    private ReceiptItemAdapter itemAdapter;

    private TextView tvSubtotal;
    private View layoutDiscount;
    private TextView tvDiscount;
    private View layoutTax;
    private TextView tvTaxTitle;
    private TextView tvTax;
    private TextView tvTotal;
    private TextView tvPaid;
    private View layoutChange;
    private TextView tvChange;
    private TextView tvPaymentMethods;
    private TextView tvFooter;

    private MaterialButton btnShareReceipt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        billingRepository = new BillingRepository(this);
        billId = getIntent().getLongExtra("billId", -1L);

        initViews();
        if (billId > 0) {
            loadReceipt();
        }
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarReceipt);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvRestaurantName = findViewById(R.id.tvReceiptRestaurantName);
        tvBranchInfo = findViewById(R.id.tvReceiptBranchInfo);
        tvTaxInfo = findViewById(R.id.tvReceiptTaxInfo);
        tvReceiptNumber = findViewById(R.id.tvReceiptNumber);
        tvReceiptDate = findViewById(R.id.tvReceiptDate);
        tvOrderTable = findViewById(R.id.tvReceiptOrderTable);
        tvCashier = findViewById(R.id.tvReceiptCashier);

        recyclerItems = findViewById(R.id.recyclerReceiptItems);
        recyclerItems.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ReceiptItemAdapter(this);
        recyclerItems.setAdapter(itemAdapter);

        tvSubtotal = findViewById(R.id.tvReceiptSubtotal);
        layoutDiscount = findViewById(R.id.layoutReceiptDiscount);
        tvDiscount = findViewById(R.id.tvReceiptDiscount);
        layoutTax = findViewById(R.id.layoutReceiptTax);
        tvTaxTitle = findViewById(R.id.tvReceiptTaxTitle);
        tvTax = findViewById(R.id.tvReceiptTax);
        tvTotal = findViewById(R.id.tvReceiptTotal);
        tvPaid = findViewById(R.id.tvReceiptPaid);
        layoutChange = findViewById(R.id.layoutReceiptChange);
        tvChange = findViewById(R.id.tvReceiptChange);
        tvPaymentMethods = findViewById(R.id.tvReceiptPaymentMethods);
        tvFooter = findViewById(R.id.tvReceiptFooter);

        btnShareReceipt = findViewById(R.id.btnShareReceipt);
        btnShareReceipt.setOnClickListener(v -> shareReceiptText());
    }

    private void loadReceipt() {
        billingRepository.getReceipt(billId, new BillingRepository.BillingCallback<>() {
            @Override
            public void onSuccess(Receipt receipt) {
                displayReceipt(receipt);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ReceiptActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayReceipt(Receipt receipt) {
        tvRestaurantName.setText(receipt.getRestaurantName());
        tvBranchInfo.setText(receipt.getBranchName() + " • " + receipt.getAddress() + "\nSimu: " + receipt.getPhone());
        tvTaxInfo.setText("TIN: " + receipt.getTinNumber() + " | VRN: " + receipt.getVrnNumber());

        tvReceiptNumber.setText("Risiti: " + receipt.getReceiptNumber());
        tvReceiptDate.setText(receipt.getReceiptDate() != null ? receipt.getReceiptDate().replace("T", " ").substring(0, 16) : "");

        String tableStr = receipt.getTableNumber() != null ? "Meza: " + receipt.getTableNumber() : receipt.getOrderType();
        tvOrderTable.setText(tableStr + " • Oda: #" + receipt.getOrderNumber());
        tvCashier.setText("Hazina: " + receipt.getCashierName());

        itemAdapter.setItems(receipt.getItems());

        tvSubtotal.setText(formatCurrency(receipt.getSubtotal()));

        if (receipt.getDiscountAmount() != null && receipt.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            layoutDiscount.setVisibility(View.VISIBLE);
            tvDiscount.setText("-" + formatCurrency(receipt.getDiscountAmount()));
        } else {
            layoutDiscount.setVisibility(View.GONE);
        }

        if (receipt.getTaxAmount() != null && receipt.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            layoutTax.setVisibility(View.VISIBLE);
            tvTaxTitle.setText("Kodi (VAT " + receipt.getTaxRate() + "%):");
            tvTax.setText(formatCurrency(receipt.getTaxAmount()));
        } else {
            layoutTax.setVisibility(View.GONE);
        }

        tvTotal.setText(formatCurrency(receipt.getTotalAmount()));
        tvPaid.setText(formatCurrency(receipt.getAmountPaid()));

        if (receipt.getChangeGiven() != null && receipt.getChangeGiven().compareTo(BigDecimal.ZERO) > 0) {
            layoutChange.setVisibility(View.VISIBLE);
            tvChange.setText(formatCurrency(receipt.getChangeGiven()));
        } else {
            layoutChange.setVisibility(View.GONE);
        }

        if (receipt.getPaymentMethodsSummary() != null && !receipt.getPaymentMethodsSummary().isEmpty()) {
            tvPaymentMethods.setText("Njia ya Malipo: " + receipt.getPaymentMethodsSummary());
        }

        if (receipt.getFooterMessage() != null) {
            tvFooter.setText(receipt.getFooterMessage());
        }
    }

    private void shareReceiptText() {
        String shareBody = tvRestaurantName.getText() + "\n" +
                tvBranchInfo.getText() + "\n" +
                tvReceiptNumber.getText() + "\n" +
                tvOrderTable.getText() + "\n" +
                "JUMLA KUU: " + tvTotal.getText() + "\n" +
                "KILICHOLIPWA: " + tvPaid.getText() + "\n" +
                tvFooter.getText();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sendIntent.setType("text/plain");

        startActivity(Intent.createChooser(sendIntent, "Shiriki Risiti"));
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "TZS 0";
        return "TZS " + String.format(Locale.getDefault(), "%,.0f", amount.doubleValue());
    }
}
