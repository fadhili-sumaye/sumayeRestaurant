package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;
public class OwnerExpenseStats {
    @SerializedName("total") private BigDecimal total;
    @SerializedName("count") private long count;
    @SerializedName("byCategory") private List<OwnerExpenseCategoryStat> byCategory;
    public BigDecimal getTotal(){return total;} public void setTotal(BigDecimal v){total=v;}
    public long getCount(){return count;} public void setCount(long v){count=v;}
    public List<OwnerExpenseCategoryStat> getByCategory(){return byCategory;} public void setByCategory(List<OwnerExpenseCategoryStat> v){byCategory=v;}
}