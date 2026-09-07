package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.ExpenseCategory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExpenseCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private boolean active;

    public static ExpenseCategoryResponse from(ExpenseCategory category) {
        return ExpenseCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.isActive())
                .build();
    }
}
