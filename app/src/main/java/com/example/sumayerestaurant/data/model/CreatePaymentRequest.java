package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;

public class CreatePaymentRequest implements Serializable {
    @SerializedName("amount")
    private BigDecimal amount;

    @SerializedName("paymentMethod")
    private String paymentMethod; // CASH, MOBILE_MONEY, CARD, BANK, OTHER

    @SerializedName("provider")
    private String provider; // NONE, MPESA, AIRTEL_MONEY, MIXX, HALOPESA, VISA, MASTERCARD

    @SerializedName("transactionReference")
    private String transactionReference;

    @SerializedName("cashReceived")
    private BigDecimal cashReceived;

    @SerializedName("idempotencyKey")
    private String idempotencyKey;

    @SerializedName("notes")
    private String notes;

    public CreatePaymentRequest(BigDecimal amount, String paymentMethod, String provider, String transactionReference, BigDecimal cashReceived, String idempotencyKey, String notes) {
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.provider = provider;
        this.transactionReference = transactionReference;
        this.cashReceived = cashReceived;
        this.idempotencyKey = idempotencyKey;
        this.notes = notes;
    }

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

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
