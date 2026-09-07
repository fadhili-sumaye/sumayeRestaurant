package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.dto.AssignDeliveryRequest;
import com.sumaye.restaurant.dto.DeliveryFeeSettingRequest;
import com.sumaye.restaurant.dto.DeliveryRequest;
import com.sumaye.restaurant.model.DeliveryFeeSetting;
import com.sumaye.restaurant.model.DeliveryAssignment;
import com.sumaye.restaurant.model.DeliveryOrder;
import com.sumaye.restaurant.service.DeliveryService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/branches/{branchId}/deliveries")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WAITER','CASHIER','KITCHEN','DELIVERY')")
    public List<DeliveryOrder> list(@PathVariable Long branchId,
                                    @RequestParam(required = false) DeliveryOrder.Status status) {
        return service.list(branchId, status);
    }

    @GetMapping("/my-deliveries")
    @PreAuthorize("hasAnyRole('DELIVERY','OWNER','ADMIN','MANAGER')")
    public List<DeliveryOrder> listMyDeliveries(@PathVariable Long branchId, Principal principal) {
        return service.listMyDeliveries(branchId, principal.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WAITER','CASHIER','KITCHEN','DELIVERY')")
    public DeliveryOrder getById(@PathVariable Long branchId, @PathVariable Long id, Principal principal) {
        return service.getDeliveryById(branchId, id, principal.getName());
    }

    @GetMapping("/{id}/assignments")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WAITER','CASHIER','KITCHEN')")
    public List<DeliveryAssignment> assignmentHistory(@PathVariable Long branchId, @PathVariable Long id) {
        return service.assignmentHistory(branchId, id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WAITER')")
    public ResponseEntity<DeliveryOrder> create(@PathVariable Long branchId,
                                                @Valid @RequestBody DeliveryRequest request,
                                                Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(branchId, request, principal.getName()));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public DeliveryOrder assign(@PathVariable Long branchId, @PathVariable Long id,
                                @Valid @RequestBody AssignDeliveryRequest request,
                                Principal principal) {
        return service.assign(branchId, id, request, principal.getName());
    }

    @PutMapping("/{id}/status/{status}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','KITCHEN','DELIVERY')")
    public DeliveryOrder status(@PathVariable Long branchId, @PathVariable Long id,
                                @PathVariable DeliveryOrder.Status status,
                                Principal principal) {
        return service.status(branchId, id, status, principal.getName());
    }

    @GetMapping("/fee-setting")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WAITER','CASHIER')")
    public DeliveryFeeSetting feeSetting(@PathVariable Long branchId) {
        return service.feeSetting(branchId);
    }

    @PutMapping("/fee-setting")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public DeliveryFeeSetting updateFeeSetting(@PathVariable Long branchId,
                                               @Valid @RequestBody DeliveryFeeSettingRequest request) {
        return service.updateFeeSetting(branchId, request);
    }
}
