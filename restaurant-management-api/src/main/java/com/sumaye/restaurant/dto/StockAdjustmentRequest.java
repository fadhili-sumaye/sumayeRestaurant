package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Ingredient;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class StockAdjustmentRequest {

    @NotNull(message = "Kitambulisho cha kiungo kinahitajika")
    private Long ingredientId;

    @NotNull(message = "Kiasi kinahitajika")
    @DecimalMin(value = "0.0001", message = "Kiasi lazima kiwe zaidi ya sifuri")
    private BigDecimal quantity;

    @NotNull(message = "Kitengo kinahitajika")
    private Ingredient.IngredientUnit unit;

    @NotNull(message = "Aina ya marekebisho inahitajika")
    private AdjustmentType adjustmentType;

    private String reason;

    public enum AdjustmentType {
        IN, OUT
    }
}
