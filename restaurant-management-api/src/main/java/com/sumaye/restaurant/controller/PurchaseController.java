package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.dto.*;
import com.sumaye.restaurant.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches/{branchId}/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER', 'STOREKEEPER')")
    public ResponseEntity<List<PurchaseResponse>> getAll(@PathVariable Long branchId) {
        return ResponseEntity.ok(purchaseService.getPurchasesByBranch(branchId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER', 'STOREKEEPER')")
    public ResponseEntity<PurchaseResponse> create(
            @PathVariable Long branchId,
            @Valid @RequestBody CreatePurchaseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchaseService.createPurchase(branchId, request, userDetails.getUsername()));
    }

    @PutMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER', 'STOREKEEPER')")
    public ResponseEntity<PurchaseResponse> receive(
            @PathVariable Long branchId,
            @PathVariable Long id,
            @Valid @RequestBody ReceivePurchaseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(purchaseService.receivePurchase(id, request, userDetails.getUsername()));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<PurchaseResponse> cancel(
            @PathVariable Long branchId,
            @PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.cancelPurchase(id));
    }
}
