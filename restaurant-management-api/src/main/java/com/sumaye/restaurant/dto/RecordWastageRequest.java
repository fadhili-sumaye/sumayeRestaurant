package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Ingredient;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class RecordWastageRequest {

    @NotNull(message = "Kitambulisho cha kiungo kinahitajika")
    private Long ingredientId;

    @NotNull(message = "Kiasi kinahitajika")
    @DecimalMin(value = "0.0001", message = "Kiasi lazima kiwe zaidi ya sifuri")
    private BigDecimal quantity;

    @NotNull(message = "Kitengo kinahitajika")
    private Ingredient.IngredientUnit unit;

    @NotBlank(message = "Sababu ya upotevu inahitajika")
    private String reason;

    private LocalDate wastageDate;
}
