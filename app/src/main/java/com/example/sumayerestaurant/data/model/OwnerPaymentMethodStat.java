package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
public class OwnerPaymentMethodStat {
    @SerializedName("paymentMethod") private String paymentMethod;
    @SerializedName("provider") private String provider;
    @SerializedName("count") private long count;
    @SerializedName("total") private BigDecimal total;
    public String getPaymentMethod(){return paymentMethod;} public void setPaymentMethod(String v){paymentMethod=v;}
    public String getProvider(){return provider;} public void setProvider(String v){provider=v;}
    public long getCount(){return count;} public void setCount(long v){count=v;}
    public BigDecimal getTotal(){return total;} public void setTotal(BigDecimal v){total=v;}
}