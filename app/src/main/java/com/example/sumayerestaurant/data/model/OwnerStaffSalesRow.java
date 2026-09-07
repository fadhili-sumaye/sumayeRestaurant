package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
public class OwnerStaffSalesRow {
    @SerializedName("username") private String username;
    @SerializedName("orders") private long orders;
    @SerializedName("sales") private BigDecimal sales;
    public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public long getOrders(){return orders;} public void setOrders(long v){orders=v;}
    public BigDecimal getSales(){return sales;} public void setSales(BigDecimal v){sales=v;}
}