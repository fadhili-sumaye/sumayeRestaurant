package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
public class OwnerStaffPaymentRow {
    @SerializedName("username") private String username;
    @SerializedName("count") private long count;
    @SerializedName("total") private BigDecimal total;
    public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public long getCount(){return count;} public void setCount(long v){count=v;}
    public BigDecimal getTotal(){return total;} public void setTotal(BigDecimal v){total=v;}
}