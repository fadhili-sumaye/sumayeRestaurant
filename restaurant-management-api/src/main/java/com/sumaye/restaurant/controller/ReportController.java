package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.dto.ProfitLossResponse;
import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.repository.ExpenseRepository;
import com.sumaye.restaurant.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/branches/{branchId}/reports")
@RequiredArgsConstructor
public class ReportController {
    private final PaymentRepository payments;
    private final ExpenseRepository expenses;

    @GetMapping("/profit-loss")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public ProfitLossResponse profitLoss(
            @PathVariable Long branchId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        LocalDate start = from == null ? LocalDate.now() : from;
        LocalDate end = to == null ? start : to;
        if (end.isBefore(start)) {
            throw new ApiException("Tarehe ya mwisho haiwezi kuwa kabla ya tarehe ya kuanzia");
        }
        BigDecimal sales = payments.totalSuccessful(branchId, start.atStartOfDay(),
                end.plusDays(1).atStartOfDay().minusNanos(1));
        BigDecimal costs = expenses.total(branchId, start, end);
        return ProfitLossResponse.builder()
                .sales(sales)
                .operatingExpenses(costs)
                .estimatedProfit(sales.subtract(costs))
                .label("Faida Inayokadiriwa — haijakaguliwa na mhasibu")
                .build();
    }
}
