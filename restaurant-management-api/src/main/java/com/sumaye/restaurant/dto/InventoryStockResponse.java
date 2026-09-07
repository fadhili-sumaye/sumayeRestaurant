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
public class InventoryStockResponse {
    private Long id;
    private Long branchId;
    private String branchName;
    private Long ingredientId;
    private String ingredientName;
    private BigDecimal quantityOnHand;
    private Ingredient.IngredientUnit unit;
    private BigDecimal minimumStockLevel;
    private BigDecimal costPerUnit;
    private LocalDateTime lastRestockedAt;

    /**
     * Kiswahili stock status badge: "IPO" | "STOCK NDOGO" | "IMEISHA"
     */
    private String stockBadge;

    /**
     * Badge colour code: GREEN / YELLOW / RED
     */
    private String badgeColor;
}
