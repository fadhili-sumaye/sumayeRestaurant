package com.example.sumayerestaurant.ui.cashier;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.model.ApplyDiscountRequest;
import com.example.sumayerestaurant.data.model.Bill;
import com.example.sumayerestaurant.data.model.CreatePaymentRequest;
import com.example.sumayerestaurant.data.repository.BillingRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

public class PaymentActivity extends AppCompatActivity {

    private BillingRepository billingRepository;
    private Bill currentBill;
    private Long billId;

    private TextView tvHeader;
    private TextView tvSubtotal;
    private TextView tvDiscount;
    private TextView btnApplyDiscount;
    private TextView tvTaxLabel;
    private TextView tvTaxAmount;
    private TextView tvTotal;
    private TextView tvBalanceDue;

    private RadioGroup rgPaymentMethod;
    private RadioButton rbCash;
    private RadioButton rbMobileMoney;
    private RadioButton rbCard;
    private RadioButton rbBank;

    private TextInputEditText etAmountToPay;
    private View layoutCashDetails;
    private TextInputEditText etCashReceived;
    private TextView tvCalculatedChange;

    private View layoutMobileMoneyDetails;
    private Spinner spinnerMobileProvider;
    private TextInputEditText etTransactionRef;

    private MaterialButton btnConfirmPayment;
    private String idempotencyKey = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        billingRepository = new BillingRepository(this);
        currentBill = (Bill) getIntent().getSerializableExtra("bill");
        billId = getIntent().getLongExtra("billId", currentBill != null ? currentBill.getId() : -1L);

        initViews();
        if (currentBill != null) {
            displayBill(currentBill);
        } else if (billId > 0) {
            loadBill();
        }
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarPayment);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvHeader = findViewById(R.id.tvPaymentBillHeader);
        tvSubtotal = findViewById(R.id.tvPaymentSubtotal);
        tvDiscount = findViewById(R.id.tvPaymentDiscount);
        btnApplyDiscount = findViewById(R.id.btnApplyDiscountDialog);
        tvTaxLabel = findViewById(R.id.tvPaymentTaxLabel);
        tvTaxAmount = findViewById(R.id.tvPaymentTaxAmount);
        tvTotal = findViewById(R.id.tvPaymentTotal);
        tvBalanceDue = findViewById(R.id.tvPaymentBalanceDue);

        rgPaymentMethod = findViewById(R.id.radioGroupPaymentMethod);
        rbCash = findViewById(R.id.rbCash);
        rbMobileMoney = findViewById(R.id.rbMobileMoney);
        rbCard = findViewById(R.id.rbCard);
        rbBank = findViewById(R.id.rbBank);

        etAmountToPay = findViewById(R.id.etAmountToPay);
        layoutCashDetails = findViewById(R.id.layoutCashDetails);
        etCashReceived = findViewById(R.id.etCashReceived);
        tvCalculatedChange = findViewById(R.id.tvCalculatedChange);

        layoutMobileMoneyDetails = findViewById(R.id.layoutMobileMoneyDetails);
        spinnerMobileProvider = findViewById(R.id.spinnerMobileProvider);
        etTransactionRef = findViewById(R.id.etTransactionRef);

        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        // Mobile Money Providers
        String[] providers = new String[]{"MPESA", "AIRTEL_MONEY", "MIXX", "HALOPESA"};
        ArrayAdapter<String> providerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, providers);
        spinnerMobileProvider.setAdapter(providerAdapter);

        // Payment Method Selector
        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCash) {
                layoutCashDetails.setVisibility(View.VISIBLE);
                layoutMobileMoneyDetails.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbMobileMoney) {
                layoutCashDetails.setVisibility(View.GONE);
                layoutMobileMoneyDetails.setVisibility(View.VISIBLE);
            } else {
                layoutCashDetails.setVisibility(View.GONE);
                layoutMobileMoneyDetails.setVisibility(View.VISIBLE); // Use reference for Card / Bank too
            }
        });

        // Live Change Calculation
        TextWatcher changeWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int count, int after) {
                calculateChange();
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        etCashReceived.addTextChangedListener(changeWatcher);
        etAmountToPay.addTextChangedListener(changeWatcher);

        btnApplyDiscount.setOnClickListener(v -> showDiscountDialog());
        btnConfirmPayment.setOnClickListener(v -> submitPayment());
    }

    private void loadBill() {
        billingRepository.getBillById(billId, new BillingRepository.BillingCallback<>() {
            @Override
            public void onSuccess(Bill result) {
                currentBill = result;
                displayBill(result);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(PaymentActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayBill(Bill bill) {
        String tableStr = bill.getTableNumber() != null ? "Meza " + bill.getTableNumber() : bill.getOrderType();
        tvHeader.setText("ANKARA #" + bill.getBillNumber() + " • " + tableStr);

        tvSubtotal.setText(formatCurrency(bill.getSubtotal()));
        tvDiscount.setText("-" + formatCurrency(bill.getDiscountAmount()));
        tvTaxLabel.setText("Kodi (VAT " + bill.getTaxRate() + "%):");
        tvTaxAmount.setText(formatCurrency(bill.getTaxAmount()));
        tvTotal.setText(formatCurrency(bill.getTotalAmount()));
        tvBalanceDue.setText(formatCurrency(bill.getBalanceDue()));

        etAmountToPay.setText(String.valueOf(bill.getBalanceDue().intValue()));
        etCashReceived.setText(String.valueOf(bill.getBalanceDue().intValue()));
        calculateChange();
    }

    private void calculateChange() {
        try {
            String toPayStr = etAmountToPay.getText() != null ? etAmountToPay.getText().toString().trim() : "0";
            String cashStr = etCashReceived.getText() != null ? etCashReceived.getText().toString().trim() : "0";

            BigDecimal amountToPay = toPayStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(toPayStr);
            BigDecimal cashReceived = cashStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(cashStr);

            BigDecimal change = cashReceived.subtract(amountToPay);
            if (change.compareTo(BigDecimal.ZERO) >= 0) {
                tvCalculatedChange.setText(formatCurrency(change));
                tvCalculatedChange.setTextColor(ContextCompat.getColor(this, R.color.success_color));
            } else {
                tvCalculatedChange.setText("Pesa Haitoshi (" + formatCurrency(change) + ")");
                tvCalculatedChange.setTextColor(ContextCompat.getColor(this, R.color.error_color));
            }
        } catch (Exception e) {
            tvCalculatedChange.setText("TZS 0");
        }
    }

    private void showDiscountDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_apply_discount, null);
        RadioButton rbFixed = dialogView.findViewById(R.id.rbFixedDiscount);
        TextInputEditText etVal = dialogView.findViewById(R.id.etDiscountValue);
        TextInputEditText etReason = dialogView.findViewById(R.id.etDiscountReason);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Weka Punguzo", (dialog, which) -> {
                    String valStr = etVal.getText() != null ? etVal.getText().toString().trim() : "";
                    if (valStr.isEmpty()) return;

                    BigDecimal val = new BigDecimal(valStr);
                    String type = rbFixed.isChecked() ? "FIXED" : "PERCENTAGE";
                    String reason = etReason.getText() != null ? etReason.getText().toString().trim() : "";

                    ApplyDiscountRequest req = new ApplyDiscountRequest(type, val, reason);
                    billingRepository.applyDiscount(currentBill.getId(), req, new BillingRepository.BillingCallback<>() {
                        @Override
                        public void onSuccess(Bill result) {
                            currentBill = result;
                            displayBill(result);
                            Toast.makeText(PaymentActivity.this, "Punguzo limewekwa kikamilifu.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(PaymentActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Ghairi", null)
                .show();
    }

    private void submitPayment() {
        String amountStr = etAmountToPay.getText() != null ? etAmountToPay.getText().toString().trim() : "";
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Tafadhali ingiza kiasi kinacholipwa.", Toast.LENGTH_SHORT).show();
            return;
        }

        BigDecimal amount = new BigDecimal(amountStr);
        String paymentMethod = "CASH";
        String provider = "NONE";
        String ref = null;
        BigDecimal cashReceived = null;

        if (rbCash.isChecked()) {
            paymentMethod = "CASH";
            String cashStr = etCashReceived.getText() != null ? etCashReceived.getText().toString().trim() : "";
            if (cashStr.isEmpty()) {
                Toast.makeText(this, "Tafadhali ingiza fedha zilizopokelewa.", Toast.LENGTH_SHORT).show();
                return;
            }
            cashReceived = new BigDecimal(cashStr);
            if (cashReceived.compareTo(amount) < 0) {
                Toast.makeText(this, "Fedha haitoshi.", Toast.LENGTH_LONG).show();
                return;
            }
        } else if (rbMobileMoney.isChecked()) {
            paymentMethod = "MOBILE_MONEY";
            provider = (String) spinnerMobileProvider.getSelectedItem();
            ref = etTransactionRef.getText() != null ? etTransactionRef.getText().toString().trim() : "";
            if (ref.isEmpty()) {
                Toast.makeText(this, "Tafadhali ingiza namba ya muamala / reference.", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (rbCard.isChecked()) {
            paymentMethod = "CARD";
            provider = "VISA";
            ref = etTransactionRef.getText() != null ? etTransactionRef.getText().toString().trim() : "";
        } else if (rbBank.isChecked()) {
            paymentMethod = "BANK";
            ref = etTransactionRef.getText() != null ? etTransactionRef.getText().toString().trim() : "";
        }

        btnConfirmPayment.setEnabled(false);
        btnConfirmPayment.setText("Inathibitisha...");

        CreatePaymentRequest request = new CreatePaymentRequest(
                amount,
                paymentMethod,
                provider,
                ref,
                cashReceived,
                idempotencyKey,
                "Malipo ya Ankara"
        );

        billingRepository.processPayment(currentBill.getId(), request, new BillingRepository.BillingCallback<>() {
            @Override
            public void onSuccess(Bill updatedBill) {
                btnConfirmPayment.setEnabled(true);
                btnConfirmPayment.setText("Thibitisha Malipo");
                Toast.makeText(PaymentActivity.this, "✅ Malipo yamethibitishwa!", Toast.LENGTH_SHORT).show();

                // Open Receipt
                Intent intent = new Intent(PaymentActivity.this, ReceiptActivity.class);
                intent.putExtra("billId", updatedBill.getId());
                intent.putExtra("bill", updatedBill);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                btnConfirmPayment.setEnabled(true);
                btnConfirmPayment.setText("Thibitisha Malipo");
                Toast.makeText(PaymentActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "TZS 0";
        return "TZS " + String.format(Locale.getDefault(), "%,.0f", amount.doubleValue());
    }
}
