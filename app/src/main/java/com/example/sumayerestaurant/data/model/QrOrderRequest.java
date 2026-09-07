package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QrOrderRequest {
    @SerializedName("clientRequestId") private final String clientRequestId;
    @SerializedName("notes") private final String notes;
    @SerializedName("items") private final List<OrderItemRequest> items;

    public QrOrderRequest(String clientRequestId, String notes, List<OrderItemRequest> items) {
        this.clientRequestId = clientRequestId;
        this.notes = notes;
        this.items = items;
    }
}
