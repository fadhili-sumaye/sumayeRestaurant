package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.dto.*;
import com.sumaye.restaurant.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches/{branchId}/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/stock")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER', 'STOREKEEPER')")
    public ResponseEntity<List<InventoryStockResponse>> getStock(@PathVariable Long branchId) {
        return ResponseEntity.ok(inventoryService.getStockForBranch(branchId));
    }

    @GetMapping("/stock/low")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER', 'STOREKEEPER')")
    public ResponseEntity<LowStockSummaryResponse> getLowStock(@PathVariable Long branchId) {
        return ResponseEntity.ok(inventoryService.getLowStockSummary(branchId));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER', 'STOREKEEPER')")
    public ResponseEntity<LowStockSummaryResponse> getDashboard(@PathVariable Long branchId) {
        return ResponseEntity.ok(inventoryService.getLowStockSummary(branchId));
    }

    @PostMapping("/stock/adjust")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER', 'STOREKEEPER')")
    public ResponseEntity<InventoryStockResponse> adjustStock(
            @PathVariable Long branchId,
            @Valid @RequestBody StockAdjustmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(inventoryService.adjustStock(branchId, request, userDetails.getUsername()));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER', 'STOREKEEPER')")
    public ResponseEntity<List<InventoryTransactionResponse>> getTransactions(@PathVariable Long branchId) {
        return ResponseEntity.ok(inventoryService.getTransactions(branchId));
    }
}
