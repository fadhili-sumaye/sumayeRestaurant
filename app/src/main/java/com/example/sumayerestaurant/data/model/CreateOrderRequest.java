package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class CreateOrderRequest implements Serializable {
    @SerializedName("tableId")
    private Long tableId;

    @SerializedName("orderType")
    private String orderType = "DINE_IN";

    @SerializedName("clientRequestId")
    private String clientRequestId;

    @SerializedName("notes")
    private String notes;

    @SerializedName("items")
    private List<OrderItemRequest> items;

    public CreateOrderRequest() {}

    public CreateOrderRequest(Long tableId, String orderType, String clientRequestId, String notes, List<OrderItemRequest> items) {
        this.tableId = tableId;
        this.orderType = orderType;
        this.clientRequestId = clientRequestId;
        this.notes = notes;
        this.items = items;
    }

    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getClientRequestId() { return clientRequestId; }
    public void setClientRequestId(String clientRequestId) { this.clientRequestId = clientRequestId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}
