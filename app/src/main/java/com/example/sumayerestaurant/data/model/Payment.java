package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;

public class Payment implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("paymentNumber")
    private String paymentNumber;

    @SerializedName("billId")
    private Long billId;

    @SerializedName("billNumber")
    private String billNumber;

    @SerializedName("orderId")
    private Long orderId;

    @SerializedName("orderNumber")
    private String orderNumber;

    @SerializedName("amount")
    private BigDecimal amount;

    @SerializedName("paymentMethod")
    private String paymentMethod; // CASH, MOBILE_MONEY, CARD, BANK, OTHER

    @SerializedName("provider")
    private String provider; // MPESA, AIRTEL_MONEY, MIXX, HALOPESA, NONE

    @SerializedName("transactionReference")
    private String transactionReference;

    @SerializedName("cashReceived")
    private BigDecimal cashReceived;

    @SerializedName("changeGiven")
    private BigDecimal changeGiven;

    @SerializedName("status")
    private String status; // SUCCESS, PENDING, FAILED, REVERSED

    @SerializedName("cashierName")
    private String cashierName;

    @SerializedName("notes")
    private String notes;

    @SerializedName("createdAt")
    private String createdAt;

    public Payment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPaymentNumber() { return paymentNumber; }
    public void setPaymentNumber(String paymentNumber) { this.paymentNumber = paymentNumber; }

    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }

    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public BigDecimal getCashReceived() { return cashReceived; }
    public void setCashReceived(BigDecimal cashReceived) { this.cashReceived = cashReceived; }

    public BigDecimal getChangeGiven() { return changeGiven; }
    public void setChangeGiven(BigDecimal changeGiven) { this.changeGiven = changeGiven; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getPaymentMethodLabelSwahili() {
        if (paymentMethod == null) return "Taslimu";
        switch (paymentMethod.toUpperCase()) {
            case "CASH": return "Pesa Taslimu";
            case "MOBILE_MONEY": return "Pesa za Simu (" + (provider != null ? provider : "") + ")";
            case "CARD": return "Kadi ya Benki";
            case "BANK": return "Benki";
            default: return paymentMethod;
        }
    }
}
