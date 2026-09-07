package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class Recipe {
    @SerializedName("id")
    private Long id;

    @SerializedName("menuItemId")
    private Long menuItemId;

    @SerializedName("menuItemName")
    private String menuItemName;

    @SerializedName("name")
    private String name;

    @SerializedName("active")
    private boolean active;

    @SerializedName("items")
    private List<RecipeItem> items;

    @SerializedName("estimatedCost")
    private BigDecimal estimatedCost;

    // Getters & Setters
    public Long getId() { return id; }
    public Long getMenuItemId() { return menuItemId; }
    public String getMenuItemName() { return menuItemName; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
    public List<RecipeItem> getItems() { return items; }
    public BigDecimal getEstimatedCost() { return estimatedCost; }
}
