package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    @SerializedName("id")
    private Long id;

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

    @SerializedName("waiterId")
    private Long waiterId;

    @SerializedName("waiterName")
    private String waiterName;

    @SerializedName("orderType")
    private String orderType;

    @SerializedName("orderSource")
    private String orderSource;

    @SerializedName("status")
    private String status;

    @SerializedName("subtotal")
    private double subtotal;

    @SerializedName("tax")
    private double tax;

    @SerializedName("discount")
    private double discount;

    @SerializedName("totalAmount")
    private double totalAmount;

    @SerializedName("notes")
    private String notes;

    @SerializedName("cancellationReason")
    private String cancellationReason;

    @SerializedName("cancelledByName")
    private String cancelledByName;

    @SerializedName("cancelledAt")
    private String cancelledAt;

    @SerializedName("items")
    private List<OrderItem> items;

    @SerializedName("createdAt")
    private String createdAt;

    public Order() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Long getWaiterId() { return waiterId; }
    public void setWaiterId(Long waiterId) { this.waiterId = waiterId; }

    public String getWaiterName() { return waiterName; }
    public void setWaiterName(String waiterName) { this.waiterName = waiterName; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getOrderSource() { return orderSource; }
    public void setOrderSource(String orderSource) { this.orderSource = orderSource; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public String getCancelledByName() { return cancelledByName; }
    public void setCancelledByName(String cancelledByName) { this.cancelledByName = cancelledByName; }

    public String getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(String cancelledAt) { this.cancelledAt = cancelledAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getStatusLabelSwahili() {
        if (status == null) return "Haijulikani";
        switch (status.toUpperCase()) {
            case "NEW":
                return "Mpya";
            case "SENT_TO_KITCHEN":
                return "Imetumwa Jikoni";
            case "ACCEPTED":
                return "Imepokelewa";
            case "PREPARING":
                return "Inaandaliwa";
            case "READY":
                return "Tayari";
            case "SERVED":
                return "Imehudumiwa";
            case "COMPLETED":
                return "Imekamilika";
            case "CANCELLED":
                return "Imeghairiwa";
            default:
                return status;
        }
    }
}
