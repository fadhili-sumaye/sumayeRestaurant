package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {
    private Long id;
    private Long branchId;
    private String branchName;
    private Long supplierId;
    private String supplierName;
    private String referenceNumber;
    private LocalDate purchaseDate;
    private BigDecimal totalCost;
    private String status;
    private String notes;
    private String receivedByName;
    private LocalDateTime receivedAt;
    private List<PurchaseItemResponse> items;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseItemResponse {
        private Long id;
        private Long ingredientId;
        private String ingredientName;
        private BigDecimal quantityOrdered;
        private BigDecimal quantityReceived;
        private Ingredient.IngredientUnit unit;
        private BigDecimal unitCost;
        private BigDecimal totalCost;
    }
}
