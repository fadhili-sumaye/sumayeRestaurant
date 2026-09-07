package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;

public class ReceiptItem implements Serializable {
    @SerializedName("itemName")
    private String itemName;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("unitPrice")
    private BigDecimal unitPrice;

    @SerializedName("subtotal")
    private BigDecimal subtotal;

    public ReceiptItem() {}

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice != null ? unitPrice : BigDecimal.ZERO; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getSubtotal() { return subtotal != null ? subtotal : BigDecimal.ZERO; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
