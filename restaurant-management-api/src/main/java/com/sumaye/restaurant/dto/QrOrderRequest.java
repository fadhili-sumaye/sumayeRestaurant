package com.sumaye.restaurant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class QrOrderRequest {
    @NotBlank
    private String clientRequestId;
    private String notes;
    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
}
