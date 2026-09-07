package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class KitchenOrder implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("orderId")
    private Long orderId;

    @SerializedName("orderNumber")
    private String orderNumber;

    @SerializedName("branchId")
    private Long branchId;

    @SerializedName("branchName")
    private String branchName;

    @SerializedName("tableId")
    private Long tableId;

    @SerializedName("tableNumber")
    private Integer tableNumber;

    @SerializedName("waiterName")
    private String waiterName;

    @SerializedName("orderType")
    private String orderType;

    @SerializedName("status")
    private String status; // NEW, ACCEPTED, PREPARING, READY, CANCELLED

    @SerializedName("notes")
    private String notes;

    @SerializedName("items")
    private List<KitchenOrderItem> items;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("acceptedAt")
    private String acceptedAt;

    @SerializedName("preparingAt")
    private String preparingAt;

    @SerializedName("readyAt")
    private String readyAt;

    @SerializedName("cancelledAt")
    private String cancelledAt;

    @SerializedName("kitchenUserName")
    private String kitchenUserName;

    public KitchenOrder() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }

    public Integer getTableNumber() { return tableNumber; }
    public void setTableNumber(Integer tableNumber) { this.tableNumber = tableNumber; }

    public String getWaiterName() { return waiterName; }
    public void setWaiterName(String waiterName) { this.waiterName = waiterName; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<KitchenOrderItem> getItems() { return items; }
    public void setItems(List<KitchenOrderItem> items) { this.items = items; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(String acceptedAt) { this.acceptedAt = acceptedAt; }

    public String getPreparingAt() { return preparingAt; }
    public void setPreparingAt(String preparingAt) { this.preparingAt = preparingAt; }

    public String getReadyAt() { return readyAt; }
    public void setReadyAt(String readyAt) { this.readyAt = readyAt; }

    public String getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(String cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getKitchenUserName() { return kitchenUserName; }
    public void setKitchenUserName(String kitchenUserName) { this.kitchenUserName = kitchenUserName; }

    public String getStatusLabelSwahili() {
        if (status == null) return "Oda Mpya";
        switch (status.toUpperCase()) {
            case "NEW": return "Oda Mpya";
            case "ACCEPTED": return "Imepokelewa";
            case "PREPARING": return "Inaandaliwa";
            case "READY": return "Tayari";
            case "CANCELLED": return "Imeghairiwa";
            default: return status;
        }
    }

    public long getElapsedMinutes() {
        if (createdAt == null) return 0;
        try {
            LocalDateTime created = LocalDateTime.parse(createdAt);
            long diff = java.time.Duration.between(created, LocalDateTime.now()).toMinutes();
            return Math.max(0, diff);
        } catch (Exception e) {
            return 0;
        }
    }
}
