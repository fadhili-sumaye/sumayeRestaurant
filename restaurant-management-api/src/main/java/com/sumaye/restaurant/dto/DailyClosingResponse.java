package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.DailyClosing;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyClosingResponse {
    private Long id;
    private LocalDate businessDate;
    private BigDecimal totalSales;
    private BigDecimal cashSales;
    private BigDecimal mobileMoneySales;
    private BigDecimal cardSales;
    private BigDecimal bankSales;
    private BigDecimal otherSales;
    private BigDecimal expenses;
    private BigDecimal expectedCash;
    private LocalDateTime closedAt;
    private String notes;

    public static DailyClosingResponse from(DailyClosing closing) {
        return DailyClosingResponse.builder()
                .id(closing.getId())
                .businessDate(closing.getBusinessDate())
                .totalSales(closing.getTotalSales())
                .cashSales(closing.getCashSales())
                .mobileMoneySales(closing.getMobileMoneySales())
                .cardSales(closing.getCardSales())
                .bankSales(closing.getBankSales())
                .otherSales(closing.getOtherSales())
                .expenses(closing.getExpenses())
                .expectedCash(closing.getExpectedCash())
                .closedAt(closing.getClosedAt())
                .notes(closing.getNotes())
                .build();
    }
}
