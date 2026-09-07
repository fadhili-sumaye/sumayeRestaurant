package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class RestaurantTable implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("tableNumber")
    private Integer tableNumber;

    @SerializedName("capacity")
    private Integer capacity;

    @SerializedName("status")
    private String status; // AVAILABLE, OCCUPIED, RESERVED, CLEANING

    @SerializedName("qrToken")
    private String qrToken;

    public RestaurantTable() {}

    public RestaurantTable(Long id, Integer tableNumber, Integer capacity, String status) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getTableNumber() { return tableNumber; }
    public void setTableNumber(Integer tableNumber) { this.tableNumber = tableNumber; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(status);
    }

    public String getStatusLabelSwahili() {
        if (status == null) return "Haijulikani";
        switch (status.toUpperCase()) {
            case "AVAILABLE":
                return "Wazi";
            case "OCCUPIED":
                return "Ina Mteja";
            case "RESERVED":
                return "Imehifadhiwa";
            case "CLEANING":
                return "Inasafishwa";
            default:
                return status;
        }
    }

    @Override
    public String toString() {
        return "Meza " + tableNumber + " — watu " + capacity + " (" + getStatusLabelSwahili() + ")";
    }
}
