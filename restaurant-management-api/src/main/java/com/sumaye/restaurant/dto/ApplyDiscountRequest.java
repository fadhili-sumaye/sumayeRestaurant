package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Bill;
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
public class ApplyDiscountRequest {
    @NotNull(message = "Aina ya punguzo inahitajika.")
    private Bill.DiscountType discountType;

    @NotNull(message = "Kiwango cha punguzo kinahitajika.")
    @DecimalMin(value = "0.01", message = "Punguzo lazima liwe zaidi ya 0.")
    private BigDecimal discountValue;

    private String reason;
}
