package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
public class OwnerCategorySales {
    @SerializedName("category") private String category;
    @SerializedName("sales") private BigDecimal sales;
    public String getCategory(){return category;} public void setCategory(String v){category=v;}
    public BigDecimal getSales(){return sales;} public void setSales(BigDecimal v){sales=v;}
}