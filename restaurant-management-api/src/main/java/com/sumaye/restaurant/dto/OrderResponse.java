package com.sumaye.restaurant.dto;

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
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long branchId;
    private String branchName;
    private Long tableId;
    private Integer tableNumber;
    private Long waiterId;
    private String waiterName;
    private Order.OrderType orderType;
    private Order.OrderSource orderSource;
    private Order.OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private String notes;
    private String clientRequestId;
    private String cancellationReason;
    private String cancelledByName;
    private LocalDateTime cancelledAt;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
