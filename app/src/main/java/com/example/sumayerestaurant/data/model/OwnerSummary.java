package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;
public class OwnerSummary {
    @SerializedName("sales") private BigDecimal sales;
    @SerializedName("expenses") private BigDecimal expenses;
    @SerializedName("profit") private BigDecimal profit;
    @SerializedName("loss") private BigDecimal loss;
    @SerializedName("totalOrders") private long totalOrders;
    @SerializedName("pendingOrders") private long pendingOrders;
    @SerializedName("completedOrders") private long completedOrders;
    @SerializedName("cancelledOrders") private long cancelledOrders;
    @SerializedName("avgOrderValue") private BigDecimal avgOrderValue;
    @SerializedName("foodSales") private BigDecimal foodSales;
    @SerializedName("beverageSales") private BigDecimal beverageSales;
    @SerializedName("otherSales") private BigDecimal otherSales;
    @SerializedName("lowStockItems") private long lowStockItems;
    @SerializedName("salesByCategory") private List<OwnerCategorySales> salesByCategory;
    public BigDecimal getSales(){return sales;} public void setSales(BigDecimal v){sales=v;}
    public BigDecimal getExpenses(){return expenses;} public void setExpenses(BigDecimal v){expenses=v;}
    public BigDecimal getProfit(){return profit;} public void setProfit(BigDecimal v){profit=v;}
    public BigDecimal getLoss(){return loss;} public void setLoss(BigDecimal v){loss=v;}
    public long getTotalOrders(){return totalOrders;} public void setTotalOrders(long v){totalOrders=v;}
    public long getPendingOrders(){return pendingOrders;} public void setPendingOrders(long v){pendingOrders=v;}
    public long getCompletedOrders(){return completedOrders;} public void setCompletedOrders(long v){completedOrders=v;}
    public long getCancelledOrders(){return cancelledOrders;} public void setCancelledOrders(long v){cancelledOrders=v;}
    public BigDecimal getAvgOrderValue(){return avgOrderValue;} public void setAvgOrderValue(BigDecimal v){avgOrderValue=v;}
    public BigDecimal getFoodSales(){return foodSales;} public void setFoodSales(BigDecimal v){foodSales=v;}
    public BigDecimal getBeverageSales(){return beverageSales;} public void setBeverageSales(BigDecimal v){beverageSales=v;}
    public BigDecimal getOtherSales(){return otherSales;} public void setOtherSales(BigDecimal v){otherSales=v;}
    public long getLowStockItems(){return lowStockItems;} public void setLowStockItems(long v){lowStockItems=v;}
    public List<OwnerCategorySales> getSalesByCategory(){return salesByCategory;} public void setSalesByCategory(List<OwnerCategorySales> v){salesByCategory=v;}
}