package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class KitchenOrderItem implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("orderItemId")
    private Long orderItemId;

    @SerializedName("itemName")
    private String itemName;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("specialInstructions")
    private String specialInstructions;

    @SerializedName("status")
    private String status; // NEW, PREPARING, READY, CANCELLED

    public KitchenOrderItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusLabelSwahili() {
        if (status == null) return "Mpya";
        switch (status.toUpperCase()) {
            case "NEW": return "Oda Mpya";
            case "PREPARING": return "Inaandaliwa";
            case "READY": return "Tayari";
            case "CANCELLED": return "Imeghairiwa";
            default: return status;
        }
    }
}
