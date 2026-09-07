package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class DailyClosing {
    @SerializedName("businessDate") private String businessDate;
    @SerializedName("totalSales") private BigDecimal totalSales;
    @SerializedName("cashSales") private BigDecimal cashSales;
    @SerializedName("mobileMoneySales") private BigDecimal mobileMoneySales;
    @SerializedName("cardSales") private BigDecimal cardSales;
    @SerializedName("bankSales") private BigDecimal bankSales;
    @SerializedName("otherSales") private BigDecimal otherSales;
    @SerializedName("expenses") private BigDecimal expenses;
    @SerializedName("expectedCash") private BigDecimal expectedCash;

    public String getBusinessDate() { return businessDate; }
    public BigDecimal getTotalSales() { return totalSales; }
    public BigDecimal getCashSales() { return cashSales; }
    public BigDecimal getMobileMoneySales() { return mobileMoneySales; }
    public BigDecimal getCardSales() { return cardSales; }
    public BigDecimal getBankSales() { return bankSales; }
    public BigDecimal getOtherSales() { return otherSales; }
    public BigDecimal getExpenses() { return expenses; }
    public BigDecimal getExpectedCash() { return expectedCash; }
}
