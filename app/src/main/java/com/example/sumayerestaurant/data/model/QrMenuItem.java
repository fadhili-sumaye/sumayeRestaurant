package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;

public class QrMenuItem {
    @SerializedName("id") private Long id;
    @SerializedName("categoryId") private Long categoryId;
    @SerializedName("categoryName") private String categoryName;
    @SerializedName("name") private String name;
    @SerializedName("description") private String description;
    @SerializedName("price") private double price;
    @SerializedName("preparationTimeMinutes") private Integer preparationTimeMinutes;

    public Long getId() { return id; }
    public String getCategoryName() { return categoryName; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public Integer getPreparationTimeMinutes() { return preparationTimeMinutes; }
}
