package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionResponse {
    private Long id;
    private Long branchId;
    private Long ingredientId;
    private String ingredientName;
    private String transactionType;
    private BigDecimal quantityChange;
    private BigDecimal quantityBefore;
    private BigDecimal quantityAfter;
    private Ingredient.IngredientUnit unit;
    private String referenceType;
    private Long referenceId;
    private String notes;
    private String createdByName;
    private LocalDateTime createdAt;
}
