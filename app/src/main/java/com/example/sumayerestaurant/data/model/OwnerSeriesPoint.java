package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
public class OwnerSeriesPoint {
    @SerializedName("label") private String label;
    @SerializedName("revenue") private BigDecimal revenue;
    @SerializedName("expenses") private BigDecimal expenses;
    @SerializedName("profit") private BigDecimal profit;
    public String getLabel(){return label;} public void setLabel(String v){label=v;}
    public BigDecimal getRevenue(){return revenue;} public void setRevenue(BigDecimal v){revenue=v;}
    public BigDecimal getExpenses(){return expenses;} public void setExpenses(BigDecimal v){expenses=v;}
    public BigDecimal getProfit(){return profit;} public void setProfit(BigDecimal v){profit=v;}
}