package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Ingredient;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class ReceivePurchaseRequest {

    @NotNull(message = "Orodha ya bidhaa zilizopokelewa inahitajika")
    private List<ReceivedItem> items;

    @Data
    @NoArgsConstructor
    public static class ReceivedItem {
        @NotNull(message = "Kitambulisho cha kipande cha ununuzi kinahitajika")
        private Long purchaseItemId;

        @NotNull(message = "Kiasi kilichopokelewa kinahitajika")
        @DecimalMin(value = "0", message = "Kiasi lazima kiwe sifuri au zaidi")
        private BigDecimal quantityReceived;
    }
}
