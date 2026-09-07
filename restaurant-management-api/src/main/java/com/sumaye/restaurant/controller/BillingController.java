package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.dto.*;
import com.sumaye.restaurant.model.TaxSetting;
import com.sumaye.restaurant.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('CASHIER', 'MANAGER', 'ADMIN', 'OWNER')")
    public ResponseEntity<List<BillResponse>> getActiveBills(Principal principal) {
        List<BillResponse> bills = billingService.getActiveBills(principal.getName());
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CASHIER', 'MANAGER', 'ADMIN', 'OWNER', 'WAITER')")
    public ResponseEntity<BillResponse> getBillById(@PathVariable Long id, Principal principal) {
        BillResponse bill = billingService.getBillById(id, principal.getName());
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasAnyRole('CASHIER', 'MANAGER', 'ADMIN', 'OWNER', 'WAITER')")
    public ResponseEntity<BillResponse> getBillByOrderId(@PathVariable Long orderId, Principal principal) {
        BillResponse bill = billingService.getBillByOrderId(orderId, principal.getName());
        return ResponseEntity.ok(bill);
    }

    @PostMapping("/orders/{orderId}/request")
    @PreAuthorize("hasAnyRole('WAITER', 'CASHIER', 'MANAGER', 'ADMIN', 'OWNER')")
    public ResponseEntity<BillResponse> requestBill(@PathVariable Long orderId, Principal principal) {
        BillResponse response = billingService.requestBill(orderId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/discount")
    @PreAuthorize("hasAnyRole('CASHIER', 'MANAGER', 'ADMIN', 'OWNER')")
    public ResponseEntity<BillResponse> applyDiscount(@PathVariable Long id,
                                                      @Valid @RequestBody ApplyDiscountRequest request,
                                                      Principal principal) {
        BillResponse response = billingService.applyDiscount(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('CASHIER', 'MANAGER', 'ADMIN', 'OWNER')")
    public ResponseEntity<BillResponse> processPayment(@PathVariable Long id,
                                                       @Valid @RequestBody CreatePaymentRequest request,
                                                       Principal principal) {
        BillResponse response = billingService.processPayment(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/receipt")
    @PreAuthorize("hasAnyRole('CASHIER', 'MANAGER', 'ADMIN', 'OWNER', 'WAITER')")
    public ResponseEntity<ReceiptResponse> getReceipt(@PathVariable Long id, Principal principal) {
        ReceiptResponse receipt = billingService.generateReceipt(id, principal.getName());
        return ResponseEntity.ok(receipt);
    }

    @GetMapping("/branches/{branchId}/tax-settings")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    public ResponseEntity<TaxSetting> getTaxSetting(@PathVariable Long branchId, Principal principal) {
        TaxSetting setting = billingService.getTaxSettingForBranch(branchId, principal.getName());
        return ResponseEntity.ok(setting);
    }

    @PutMapping("/branches/{branchId}/tax-settings")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    public ResponseEntity<TaxSetting> updateTaxSetting(@PathVariable Long branchId,
                                                       @Valid @RequestBody UpdateTaxSettingRequest request,
                                                       Principal principal) {
        TaxSetting setting = billingService.updateTaxSetting(branchId, request.getTaxName(), request.getTaxRate(), request.getEnabled(), principal.getName());
        return ResponseEntity.ok(setting);
    }
}

