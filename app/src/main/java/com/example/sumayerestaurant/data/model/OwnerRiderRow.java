package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
public class OwnerRiderRow {
    @SerializedName("username") private String username;
    @SerializedName("delivered") private long delivered;
    @SerializedName("deliveryFees") private BigDecimal deliveryFees;
    public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public long getDelivered(){return delivered;} public void setDelivered(long v){delivered=v;}
    public BigDecimal getDeliveryFees(){return deliveryFees;} public void setDeliveryFees(BigDecimal v){deliveryFees=v;}
}