package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class StockAdjustmentRequest {
    @SerializedName("ingredientId")
    private Long ingredientId;

    @SerializedName("quantity")
    private BigDecimal quantity;

    @SerializedName("unit")
    private String unit;

    @SerializedName("adjustmentType")
    private String adjustmentType; // "IN" or "OUT"

    @SerializedName("reason")
    private String reason;

    public StockAdjustmentRequest() {}

    public StockAdjustmentRequest(Long ingredientId, BigDecimal quantity,
                                   String unit, String adjustmentType, String reason) {
        this.ingredientId = ingredientId;
        this.quantity = quantity;
        this.unit = unit;
        this.adjustmentType = adjustmentType;
        this.reason = reason;
    }

    // Getters & Setters
    public Long getIngredientId() { return ingredientId; }
    public void setIngredientId(Long ingredientId) { this.ingredientId = ingredientId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(String adjustmentType) { this.adjustmentType = adjustmentType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
