package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.dto.KitchenOrderResponse;
import com.sumaye.restaurant.dto.KitchenStatusUpdateRequest;
import com.sumaye.restaurant.service.KitchenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/kitchen")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('KITCHEN', 'MANAGER', 'ADMIN', 'OWNER')")
public class KitchenController {

    private final KitchenService kitchenService;

    @GetMapping("/orders")
    public ResponseEntity<List<KitchenOrderResponse>> getActiveOrders(Principal principal) {
        List<KitchenOrderResponse> orders = kitchenService.getActiveKitchenOrders(principal.getName());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<KitchenOrderResponse> getOrderById(@PathVariable Long id, Principal principal) {
        KitchenOrderResponse order = kitchenService.getKitchenOrderById(id, principal.getName());
        return ResponseEntity.ok(order);
    }

    @PutMapping("/orders/{id}/accept")
    public ResponseEntity<KitchenOrderResponse> acceptOrder(@PathVariable Long id, Principal principal) {
        KitchenOrderResponse response = kitchenService.acceptKitchenOrder(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/orders/{id}/preparing")
    public ResponseEntity<KitchenOrderResponse> startPreparing(@PathVariable Long id, Principal principal) {
        KitchenOrderResponse response = kitchenService.startPreparingKitchenOrder(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/orders/{id}/ready")
    public ResponseEntity<KitchenOrderResponse> markReady(@PathVariable Long id, Principal principal) {
        KitchenOrderResponse response = kitchenService.markKitchenOrderReady(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/orders/{id}/cancel")
    public ResponseEntity<KitchenOrderResponse> cancelOrder(@PathVariable Long id,
                                                            @RequestBody(required = false) KitchenStatusUpdateRequest request,
                                                            Principal principal) {
        String reason = request != null ? request.getReason() : "Imeghairiwa na jiko";
        KitchenOrderResponse response = kitchenService.cancelKitchenOrder(id, reason, principal.getName());
        return ResponseEntity.ok(response);
    }
}
