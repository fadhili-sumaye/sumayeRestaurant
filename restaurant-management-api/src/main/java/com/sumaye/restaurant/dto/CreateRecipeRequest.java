package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Ingredient;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class CreateRecipeRequest {

    @NotNull(message = "Kitambulisho cha chakula kinahitajika")
    private Long menuItemId;

    @NotBlank(message = "Jina la mapishi linahitajika")
    private String name;

    @NotNull(message = "Orodha ya viungo inahitajika")
    private List<RecipeItemRequest> items;

    @Data
    @NoArgsConstructor
    public static class RecipeItemRequest {
        @NotNull(message = "Kitambulisho cha kiungo kinahitajika")
        private Long ingredientId;

        @NotNull(message = "Kiasi kinahitajika")
        @DecimalMin(value = "0.0001", message = "Kiasi lazima kiwe zaidi ya sifuri")
        private BigDecimal quantityRequired;

        @NotNull(message = "Kitengo kinahitajika")
        private Ingredient.IngredientUnit unit;
    }
}
