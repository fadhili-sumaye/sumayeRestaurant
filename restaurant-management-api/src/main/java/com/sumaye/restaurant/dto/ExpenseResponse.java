package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Expense;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExpenseResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String description;
    private BigDecimal amount;
    private String paymentMethod;
    private String referenceNumber;
    private LocalDate expenseDate;
    private String notes;
    private String status;
    private String voidReason;

    public static ExpenseResponse from(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .categoryId(expense.getCategory().getId())
                .categoryName(expense.getCategory().getName())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .paymentMethod(expense.getPaymentMethod().name())
                .referenceNumber(expense.getReferenceNumber())
                .expenseDate(expense.getExpenseDate())
                .notes(expense.getNotes())
                .status(expense.getStatus().name())
                .voidReason(expense.getVoidReason())
                .build();
    }
}
