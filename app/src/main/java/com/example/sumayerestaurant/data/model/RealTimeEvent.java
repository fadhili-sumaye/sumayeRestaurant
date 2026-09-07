package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class RealTimeEvent implements Serializable {
    @SerializedName("eventType")
    private String eventType; // NEW_KOT, KOT_STATUS_UPDATED, ORDER_READY, ORDER_CANCELLED

    @SerializedName("branchId")
    private Long branchId;

    @SerializedName("orderId")
    private Long orderId;

    @SerializedName("orderNumber")
    private String orderNumber;

    @SerializedName("waiterUsername")
    private String waiterUsername;

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("timestamp")
    private String timestamp;

    public RealTimeEvent() {}

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getWaiterUsername() { return waiterUsername; }
    public void setWaiterUsername(String waiterUsername) { this.waiterUsername = waiterUsername; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
