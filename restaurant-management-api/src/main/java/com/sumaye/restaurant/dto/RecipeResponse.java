package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeResponse {
    private Long id;
    private Long menuItemId;
    private String menuItemName;
    private String name;
    private boolean active;
    private List<RecipeItemResponse> items;
    private BigDecimal estimatedCost;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeItemResponse {
        private Long id;
        private Long ingredientId;
        private String ingredientName;
        private BigDecimal quantityRequired;
        private Ingredient.IngredientUnit unit;
        private BigDecimal costContribution;
    }
}
