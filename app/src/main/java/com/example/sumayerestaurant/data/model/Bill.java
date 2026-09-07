package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class Bill implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("billNumber")
    private String billNumber;

    @SerializedName("orderId")
    private Long orderId;

    @SerializedName("orderNumber")
    private String orderNumber;

    @SerializedName("branchId")
    private Long branchId;

    @SerializedName("branchName")
    private String branchName;

    @SerializedName("tableId")
    private Long tableId;

    @SerializedName("tableNumber")
    private Integer tableNumber;

    @SerializedName("waiterName")
    private String waiterName;

    @SerializedName("orderType")
    private String orderType;

    @SerializedName("orderItems")
    private List<OrderItem> orderItems;

    @SerializedName("subtotal")
    private BigDecimal subtotal;

    @SerializedName("deliveryFee")
    private BigDecimal deliveryFee;

    @SerializedName("discountType")
    private String discountType; // NONE, FIXED, PERCENTAGE

    @SerializedName("discountAmount")
    private BigDecimal discountAmount;

    @SerializedName("discountReason")
    private String discountReason;

    @SerializedName("discountAppliedByName")
    private String discountAppliedByName;

    @SerializedName("taxRate")
    private BigDecimal taxRate;

    @SerializedName("taxAmount")
    private BigDecimal taxAmount;

    @SerializedName("totalAmount")
    private BigDecimal totalAmount;

    @SerializedName("amountPaid")
    private BigDecimal amountPaid;

    @SerializedName("balanceDue")
    private BigDecimal balanceDue;

    @SerializedName("paymentStatus")
    private String paymentStatus; // UNPAID, PARTIALLY_PAID, PAID, REFUNDED

    @SerializedName("cashierName")
    private String cashierName;

    @SerializedName("billRequested")
    private boolean billRequested;

    @SerializedName("billRequestedAt")
    private String billRequestedAt;

    @SerializedName("payments")
    private List<Payment> payments;

    @SerializedName("notes")
    private String notes;

    @SerializedName("createdAt")
    private String createdAt;

    public Bill() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }

    public Integer getTableNumber() { return tableNumber; }
    public void setTableNumber(Integer tableNumber) { this.tableNumber = tableNumber; }

    public String getWaiterName() { return waiterName; }
    public void setWaiterName(String waiterName) { this.waiterName = waiterName; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    public BigDecimal getSubtotal() { return subtotal != null ? subtotal : BigDecimal.ZERO; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDeliveryFee() { return deliveryFee != null ? deliveryFee : BigDecimal.ZERO; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountAmount() { return discountAmount != null ? discountAmount : BigDecimal.ZERO; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public String getDiscountReason() { return discountReason; }
    public void setDiscountReason(String discountReason) { this.discountReason = discountReason; }

    public String getDiscountAppliedByName() { return discountAppliedByName; }
    public void setDiscountAppliedByName(String discountAppliedByName) { this.discountAppliedByName = discountAppliedByName; }

    public BigDecimal getTaxRate() { return taxRate != null ? taxRate : BigDecimal.ZERO; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public BigDecimal getTaxAmount() { return taxAmount != null ? taxAmount : BigDecimal.ZERO; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount != null ? totalAmount : BigDecimal.ZERO; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getAmountPaid() { return amountPaid != null ? amountPaid : BigDecimal.ZERO; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public BigDecimal getBalanceDue() { return balanceDue != null ? balanceDue : BigDecimal.ZERO; }
    public void setBalanceDue(BigDecimal balanceDue) { this.balanceDue = balanceDue; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public boolean isBillRequested() { return billRequested; }
    public void setBillRequested(boolean billRequested) { this.billRequested = billRequested; }

    public String getBillRequestedAt() { return billRequestedAt; }
    public void setBillRequestedAt(String billRequestedAt) { this.billRequestedAt = billRequestedAt; }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getPaymentStatusLabelSwahili() {
        if (paymentStatus == null) return "Haijalipwa";
        switch (paymentStatus.toUpperCase()) {
            case "UNPAID": return "Haijalipwa";
            case "PARTIALLY_PAID": return "Imelipwa Sehemu";
            case "PAID": return "Imelipwa";
            case "REFUNDED": return "Imerudishwa";
            default: return paymentStatus;
        }
    }
}
