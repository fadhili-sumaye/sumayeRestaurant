package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.dto.ExpenseCategoryRequest;
import com.sumaye.restaurant.dto.ExpenseCategoryResponse;
import com.sumaye.restaurant.dto.ExpenseRequest;
import com.sumaye.restaurant.dto.ExpenseResponse;
import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.repository.ExpenseCategoryRepository;
import com.sumaye.restaurant.repository.ExpenseRepository;
import com.sumaye.restaurant.service.ExpenseService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/branches/{branchId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService service;
    private final ExpenseRepository expenses;
    private final ExpenseCategoryRepository categories;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','CASHIER')")
    public List<ExpenseResponse> list(
            @PathVariable Long branchId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        LocalDate start = from == null ? LocalDate.now() : from;
        LocalDate end = to == null ? start : to;
        if (end.isBefore(start)) {
            throw new ApiException("Tarehe ya mwisho haiwezi kuwa kabla ya tarehe ya kuanzia");
        }
        return expenses.findByBranchIdAndExpenseDateBetweenOrderByExpenseDateDesc(branchId, start, end).stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','CASHIER')")
    public List<ExpenseCategoryResponse> categories(@PathVariable Long branchId) {
        return categories.findByBranchIdAndActiveTrueOrderByName(branchId).stream()
                .map(ExpenseCategoryResponse::from)
                .toList();
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public ResponseEntity<ExpenseCategoryResponse> category(
            @PathVariable Long branchId,
            @Valid @RequestBody ExpenseCategoryRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ExpenseCategoryResponse.from(service.createCategory(branchId, request, user.getUsername())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public ResponseEntity<ExpenseResponse> create(
            @PathVariable Long branchId,
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ExpenseResponse.from(service.create(branchId, request, user.getUsername())));
    }

    @PutMapping("/{id}/void")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public ResponseEntity<Void> voidExpense(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails user) {
        service.voidExpense(id, reason, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
