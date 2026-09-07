package com.sumaye.restaurant.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaxSettingRequest {

    private String taxName;

    @NotNull(message = "Kiwango cha kodi kinahitajika")
    @DecimalMin(value = "0.0", message = "Kiwango cha kodi hakiwezi kuwa chini ya 0%")
    @DecimalMax(value = "100.0", message = "Kiwango cha kodi hakiwezi kuzidi 100%")
    private BigDecimal taxRate;

    @NotNull(message = "Hali ya kodi (enabled/disabled) inahitajika")
    private Boolean enabled;
}
