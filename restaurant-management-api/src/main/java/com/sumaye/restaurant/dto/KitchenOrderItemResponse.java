package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.KitchenOrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenOrderItemResponse {
    private Long id;
    private Long orderItemId;
    private String itemName;
    private Integer quantity;
    private String specialInstructions;
    private KitchenOrderItem.KitchenOrderItemStatus status;
}
