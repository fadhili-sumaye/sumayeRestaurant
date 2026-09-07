package com.sumaye.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeEvent {
    private String eventType; // NEW_KOT, KOT_STATUS_UPDATED, ORDER_READY, ORDER_CANCELLED
    private Long branchId;
    private Long orderId;
    private String orderNumber;
    private String waiterUsername;
    private String status;
    private String message;
    private Object payload;
    private LocalDateTime timestamp;
}
