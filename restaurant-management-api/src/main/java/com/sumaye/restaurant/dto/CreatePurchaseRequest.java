package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Ingredient;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class CreatePurchaseRequest {

    private Long supplierId;
    private String referenceNumber;
    private LocalDate purchaseDate;
    private String notes;

    @NotNull(message = "Orodha ya bidhaa inahitajika")
    private List<PurchaseItemRequest> items;

    @Data
    @NoArgsConstructor
    public static class PurchaseItemRequest {
        @NotNull(message = "Kitambulisho cha kiungo kinahitajika")
        private Long ingredientId;

        @NotNull(message = "Kiasi kilichoagizwa kinahitajika")
        @DecimalMin(value = "0.0001", message = "Kiasi lazima kiwe zaidi ya sifuri")
        private BigDecimal quantityOrdered;

        @NotNull(message = "Kitengo kinahitajika")
        private Ingredient.IngredientUnit unit;

        private BigDecimal unitCost;
    }
}
