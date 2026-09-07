package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class Receipt implements Serializable {
    @SerializedName("restaurantName")
    private String restaurantName;

    @SerializedName("branchName")
    private String branchName;

    @SerializedName("address")
    private String address;

    @SerializedName("phone")
    private String phone;

    @SerializedName("tinNumber")
    private String tinNumber;

    @SerializedName("vrnNumber")
    private String vrnNumber;

    @SerializedName("receiptNumber")
    private String receiptNumber;

    @SerializedName("billNumber")
    private String billNumber;

    @SerializedName("orderNumber")
    private String orderNumber;

    @SerializedName("tableNumber")
    private Integer tableNumber;

    @SerializedName("orderType")
    private String orderType;

    @SerializedName("waiterName")
    private String waiterName;

    @SerializedName("cashierName")
    private String cashierName;

    @SerializedName("items")
    private List<ReceiptItem> items;

    @SerializedName("subtotal")
    private BigDecimal subtotal;

    @SerializedName("discountAmount")
    private BigDecimal discountAmount;

    @SerializedName("taxRate")
    private BigDecimal taxRate;

    @SerializedName("taxAmount")
    private BigDecimal taxAmount;

    @SerializedName("totalAmount")
    private BigDecimal totalAmount;

    @SerializedName("amountPaid")
    private BigDecimal amountPaid;

    @SerializedName("changeGiven")
    private BigDecimal changeGiven;

    @SerializedName("balanceDue")
    private BigDecimal balanceDue;

    @SerializedName("paymentMethodsSummary")
    private String paymentMethodsSummary;

    @SerializedName("payments")
    private List<Payment> payments;

    @SerializedName("receiptDate")
    private String receiptDate;

    @SerializedName("footerMessage")
    private String footerMessage;

    public Receipt() {}

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getTinNumber() { return tinNumber; }
    public void setTinNumber(String tinNumber) { this.tinNumber = tinNumber; }

    public String getVrnNumber() { return vrnNumber; }
    public void setVrnNumber(String vrnNumber) { this.vrnNumber = vrnNumber; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Integer getTableNumber() { return tableNumber; }
    public void setTableNumber(Integer tableNumber) { this.tableNumber = tableNumber; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getWaiterName() { return waiterName; }
    public void setWaiterName(String waiterName) { this.waiterName = waiterName; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public List<ReceiptItem> getItems() { return items; }
    public void setItems(List<ReceiptItem> items) { this.items = items; }

    public BigDecimal getSubtotal() { return subtotal != null ? subtotal : BigDecimal.ZERO; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountAmount() { return discountAmount != null ? discountAmount : BigDecimal.ZERO; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTaxRate() { return taxRate != null ? taxRate : BigDecimal.ZERO; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public BigDecimal getTaxAmount() { return taxAmount != null ? taxAmount : BigDecimal.ZERO; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount != null ? totalAmount : BigDecimal.ZERO; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getAmountPaid() { return amountPaid != null ? amountPaid : BigDecimal.ZERO; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public BigDecimal getChangeGiven() { return changeGiven != null ? changeGiven : BigDecimal.ZERO; }
    public void setChangeGiven(BigDecimal changeGiven) { this.changeGiven = changeGiven; }

    public BigDecimal getBalanceDue() { return balanceDue != null ? balanceDue : BigDecimal.ZERO; }
    public void setBalanceDue(BigDecimal balanceDue) { this.balanceDue = balanceDue; }

    public String getPaymentMethodsSummary() { return paymentMethodsSummary; }
    public void setPaymentMethodsSummary(String paymentMethodsSummary) { this.paymentMethodsSummary = paymentMethodsSummary; }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }

    public String getReceiptDate() { return receiptDate; }
    public void setReceiptDate(String receiptDate) { this.receiptDate = receiptDate; }

    public String getFooterMessage() { return footerMessage; }
    public void setFooterMessage(String footerMessage) { this.footerMessage = footerMessage; }
}
