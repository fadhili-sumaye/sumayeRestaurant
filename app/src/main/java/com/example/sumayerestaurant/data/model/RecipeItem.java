package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class RecipeItem {
    @SerializedName("id")
    private Long id;

    @SerializedName("ingredientId")
    private Long ingredientId;

    @SerializedName("ingredientName")
    private String ingredientName;

    @SerializedName("quantityRequired")
    private BigDecimal quantityRequired;

    @SerializedName("unit")
    private String unit;

    @SerializedName("costContribution")
    private BigDecimal costContribution;

    // Getters
    public Long getId() { return id; }
    public Long getIngredientId() { return ingredientId; }
    public String getIngredientName() { return ingredientName; }
    public BigDecimal getQuantityRequired() { return quantityRequired; }
    public String getUnit() { return unit; }
    public BigDecimal getCostContribution() { return costContribution; }
}
