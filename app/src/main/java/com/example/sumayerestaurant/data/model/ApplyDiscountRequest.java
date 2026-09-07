package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;

public class ApplyDiscountRequest implements Serializable {
    @SerializedName("discountType")
    private String discountType; // FIXED, PERCENTAGE

    @SerializedName("discountValue")
    private BigDecimal discountValue;

    @SerializedName("reason")
    private String reason;

    public ApplyDiscountRequest(String discountType, BigDecimal discountValue, String reason) {
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.reason = reason;
    }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
