package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.KitchenOrder;
import com.sumaye.restaurant.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenOrderResponse {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long branchId;
    private String branchName;
    private Long tableId;
    private Integer tableNumber;
    private String waiterName;
    private Order.OrderType orderType;
    private KitchenOrder.KitchenOrderStatus status;
    private String notes;
    private List<KitchenOrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime preparingAt;
    private LocalDateTime readyAt;
    private LocalDateTime cancelledAt;
    private String kitchenUserName;
}
