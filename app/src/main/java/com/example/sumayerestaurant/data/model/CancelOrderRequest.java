package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CancelOrderRequest implements Serializable {
    @SerializedName("reason")
    private String reason;

    public CancelOrderRequest() {}

    public CancelOrderRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
