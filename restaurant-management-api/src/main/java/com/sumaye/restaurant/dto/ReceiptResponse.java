package com.sumaye.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponse {
    private String restaurantName;
    private String branchName;
    private String address;
    private String phone;
    private String tinNumber;
    private String vrnNumber;

    private String receiptNumber;
    private String billNumber;
    private String orderNumber;
    private Integer tableNumber;
    private String orderType;
    private String waiterName;
    private String cashierName;

    private List<ReceiptItemResponse> items;

    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal discountAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal changeGiven;
    private BigDecimal balanceDue;

    private String paymentMethodsSummary; // e.g. "CASH (TZS 20,000), MPESA (TZS 10,000)"
    private List<PaymentResponse> payments;

    private LocalDateTime receiptDate;
    private String footerMessage; // "Asante kwa kutuhudumia. Karibu Tena!"
}
