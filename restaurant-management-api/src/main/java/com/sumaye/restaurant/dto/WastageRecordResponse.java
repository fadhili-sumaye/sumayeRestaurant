package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WastageRecordResponse {
    private Long id;
    private Long branchId;
    private Long ingredientId;
    private String ingredientName;
    private BigDecimal quantity;
    private Ingredient.IngredientUnit unit;
    private String reason;
    private String recordedByName;
    private LocalDate wastageDate;
    private LocalDateTime createdAt;
}
