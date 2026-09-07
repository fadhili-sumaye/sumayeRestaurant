package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QrTableMenu {
    @SerializedName("restaurantName") private String restaurantName;
    @SerializedName("branchName") private String branchName;
    @SerializedName("tableNumber") private Integer tableNumber;
    @SerializedName("menuItems") private List<QrMenuItem> menuItems;

    public String getRestaurantName() { return restaurantName; }
    public String getBranchName() { return branchName; }
    public Integer getTableNumber() { return tableNumber; }
    public List<QrMenuItem> getMenuItems() { return menuItems; }
}
