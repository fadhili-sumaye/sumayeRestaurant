package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private Long tableId;
    private Order.OrderType orderType = Order.OrderType.DINE_IN;
    private String clientRequestId;
    private String notes;
    private List<OrderItemRequest> items;
}
