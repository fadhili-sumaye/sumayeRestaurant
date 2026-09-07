package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class InventoryStock {
    @SerializedName("id")
    private Long id;

    @SerializedName("branchId")
    private Long branchId;

    @SerializedName("branchName")
    private String branchName;

    @SerializedName("ingredientId")
    private Long ingredientId;

    @SerializedName("ingredientName")
    private String ingredientName;

    @SerializedName("quantityOnHand")
    private BigDecimal quantityOnHand;

    @SerializedName("unit")
    private String unit;

    @SerializedName("minimumStockLevel")
    private BigDecimal minimumStockLevel;

    @SerializedName("costPerUnit")
    private BigDecimal costPerUnit;

    @SerializedName("lastRestockedAt")
    private String lastRestockedAt;

    /** "IPO" | "STOCK NDOGO" | "IMEISHA" */
    @SerializedName("stockBadge")
    private String stockBadge;

    /** "GREEN" | "YELLOW" | "RED" */
    @SerializedName("badgeColor")
    private String badgeColor;

    // Getters & Setters
    public Long getId() { return id; }
    public Long getBranchId() { return branchId; }
    public String getBranchName() { return branchName; }
    public Long getIngredientId() { return ingredientId; }
    public String getIngredientName() { return ingredientName; }
    public BigDecimal getQuantityOnHand() { return quantityOnHand; }
    public String getUnit() { return unit; }
    public BigDecimal getMinimumStockLevel() { return minimumStockLevel; }
    public BigDecimal getCostPerUnit() { return costPerUnit; }
    public String getLastRestockedAt() { return lastRestockedAt; }
    public String getStockBadge() { return stockBadge; }
    public String getBadgeColor() { return badgeColor; }
}
