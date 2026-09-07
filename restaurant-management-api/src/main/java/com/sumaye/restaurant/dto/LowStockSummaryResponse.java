package com.sumaye.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockSummaryResponse {
    private Long branchId;
    private String branchName;
    private int totalLowStockItems;
    private int outOfStockItems;
    private List<InventoryStockResponse> lowStockItems;
}
