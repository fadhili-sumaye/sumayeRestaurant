package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Bill;
import com.sumaye.restaurant.model.Order;
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
public class BillResponse {
    private Long id;
    private String billNumber;
    private Long orderId;
    private String orderNumber;
    private Long branchId;
    private String branchName;
    private Long tableId;
    private Integer tableNumber;
    private String waiterName;
    private Order.OrderType orderType;
    private List<OrderItemResponse> orderItems;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private Bill.DiscountType discountType;
    private BigDecimal discountAmount;
    private String discountReason;
    private String discountAppliedByName;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private Bill.PaymentStatus paymentStatus;
    private String cashierName;
    private boolean billRequested;
    private LocalDateTime billRequestedAt;
    private List<PaymentResponse> payments;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
