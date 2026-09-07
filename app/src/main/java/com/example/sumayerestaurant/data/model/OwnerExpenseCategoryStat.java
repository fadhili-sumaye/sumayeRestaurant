package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
public class OwnerExpenseCategoryStat {
    @SerializedName("categoryId") private Long categoryId;
    @SerializedName("categoryName") private String categoryName;
    @SerializedName("amount") private BigDecimal amount;
    @SerializedName("percent") private BigDecimal percent;
    public Long getCategoryId(){return categoryId;} public void setCategoryId(Long v){categoryId=v;}
    public String getCategoryName(){return categoryName;} public void setCategoryName(String v){categoryName=v;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
    public BigDecimal getPercent(){return percent;} public void setPercent(BigDecimal v){percent=v;}
}