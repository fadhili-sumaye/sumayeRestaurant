package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
public class OwnerMonthlyPerformance {
    @SerializedName("monthLabel") private String monthLabel;
    @SerializedName("sales") private BigDecimal sales;
    @SerializedName("previousSales") private BigDecimal previousSales;
    @SerializedName("salesGrowthPercent") private BigDecimal salesGrowthPercent;
    @SerializedName("orders") private long orders;
    @SerializedName("previousOrders") private long previousOrders;
    @SerializedName("ordersGrowthPercent") private BigDecimal ordersGrowthPercent;
    @SerializedName("expenses") private BigDecimal expenses;
    @SerializedName("previousExpenses") private BigDecimal previousExpenses;
    @SerializedName("profit") private BigDecimal profit;
    @SerializedName("previousProfit") private BigDecimal previousProfit;
    @SerializedName("bestSellingFood") private String bestSellingFood;
    @SerializedName("bestSellingCategory") private String bestSellingCategory;
    public String getMonthLabel(){return monthLabel;} public void setMonthLabel(String v){monthLabel=v;}
    public BigDecimal getSales(){return sales;} public void setSales(BigDecimal v){sales=v;}
    public BigDecimal getPreviousSales(){return previousSales;} public void setPreviousSales(BigDecimal v){previousSales=v;}
    public BigDecimal getSalesGrowthPercent(){return salesGrowthPercent;} public void setSalesGrowthPercent(BigDecimal v){salesGrowthPercent=v;}
    public long getOrders(){return orders;} public void setOrders(long v){orders=v;}
    public long getPreviousOrders(){return previousOrders;} public void setPreviousOrders(long v){previousOrders=v;}
    public BigDecimal getOrdersGrowthPercent(){return ordersGrowthPercent;} public void setOrdersGrowthPercent(BigDecimal v){ordersGrowthPercent=v;}
    public BigDecimal getExpenses(){return expenses;} public void setExpenses(BigDecimal v){expenses=v;}
    public BigDecimal getPreviousExpenses(){return previousExpenses;} public void setPreviousExpenses(BigDecimal v){previousExpenses=v;}
    public BigDecimal getProfit(){return profit;} public void setProfit(BigDecimal v){profit=v;}
    public BigDecimal getPreviousProfit(){return previousProfit;} public void setPreviousProfit(BigDecimal v){previousProfit=v;}
    public String getBestSellingFood(){return bestSellingFood;} public void setBestSellingFood(String v){bestSellingFood=v;}
    public String getBestSellingCategory(){return bestSellingCategory;} public void setBestSellingCategory(String v){bestSellingCategory=v;}
}