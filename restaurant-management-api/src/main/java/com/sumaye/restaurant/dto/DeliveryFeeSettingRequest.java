package com.sumaye.restaurant.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class DeliveryFeeSettingRequest {
    @NotNull @DecimalMin("0.0") private BigDecimal fixedFee;
    @NotNull private Boolean enabled;
}
