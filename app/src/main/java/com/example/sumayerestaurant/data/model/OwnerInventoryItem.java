package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
public class OwnerInventoryItem {
    @SerializedName("id") private Long id;
    @SerializedName("ingredientName") private String ingredientName;
    @SerializedName("transactionType") private String transactionType;
    @SerializedName("quantityChange") private BigDecimal quantityChange;
    @SerializedName("unit") private String unit;
    @SerializedName("notes") private String notes;
    @SerializedName("createdAt") private String createdAt;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getIngredientName(){return ingredientName;} public void setIngredientName(String v){ingredientName=v;}
    public String getTransactionType(){return transactionType;} public void setTransactionType(String v){transactionType=v;}
    public BigDecimal getQuantityChange(){return quantityChange;} public void setQuantityChange(BigDecimal v){quantityChange=v;}
    public String getUnit(){return unit;} public void setUnit(String v){unit=v;}
    public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
    public String getCreatedAt(){return createdAt;} public void setCreatedAt(String v){createdAt=v;}
}