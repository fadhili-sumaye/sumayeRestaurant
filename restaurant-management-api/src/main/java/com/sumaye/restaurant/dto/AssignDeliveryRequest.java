package com.sumaye.restaurant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignDeliveryRequest {
    @NotNull private Long riderId;
    private String notes;
}
