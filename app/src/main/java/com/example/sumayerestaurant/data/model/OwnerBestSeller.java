package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
public class OwnerBestSeller {
    @SerializedName("menuItemId") private Long menuItemId;
    @SerializedName("name") private String name;
    @SerializedName("categoryName") private String categoryName;
    @SerializedName("quantity") private long quantity;
    @SerializedName("revenue") private BigDecimal revenue;
    public Long getMenuItemId(){return menuItemId;} public void setMenuItemId(Long v){menuItemId=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getCategoryName(){return categoryName;} public void setCategoryName(String v){categoryName=v;}
    public long getQuantity(){return quantity;} public void setQuantity(long v){quantity=v;}
    public BigDecimal getRevenue(){return revenue;} public void setRevenue(BigDecimal v){revenue=v;}
}