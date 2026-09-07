package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;
public class OwnerInventorySummary {
    @SerializedName("totalItems") private long totalItems;
    @SerializedName("lowStock") private long lowStock;
    @SerializedName("outOfStock") private long outOfStock;
    @SerializedName("stockValue") private BigDecimal stockValue;
    @SerializedName("wastageCount") private long wastageCount;
    @SerializedName("wastageQuantity") private BigDecimal wastageQuantity;
    @SerializedName("recentTransactions") private List<OwnerInventoryItem> recentTransactions;
    public long getTotalItems(){return totalItems;} public void setTotalItems(long v){totalItems=v;}
    public long getLowStock(){return lowStock;} public void setLowStock(long v){lowStock=v;}
    public long getOutOfStock(){return outOfStock;} public void setOutOfStock(long v){outOfStock=v;}
    public BigDecimal getStockValue(){return stockValue;} public void setStockValue(BigDecimal v){stockValue=v;}
    public long getWastageCount(){return wastageCount;} public void setWastageCount(long v){wastageCount=v;}
    public BigDecimal getWastageQuantity(){return wastageQuantity;} public void setWastageQuantity(BigDecimal v){wastageQuantity=v;}
    public List<OwnerInventoryItem> getRecentTransactions(){return recentTransactions;} public void setRecentTransactions(List<OwnerInventoryItem> v){recentTransactions=v;}
}