package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.dto.CancelOrderRequest;
import com.sumaye.restaurant.dto.CreateOrderRequest;
import com.sumaye.restaurant.dto.OrderResponse;
import com.sumaye.restaurant.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('WAITER', 'MANAGER', 'ADMIN', 'OWNER')")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request,
                                                     Principal principal) {
        OrderResponse response = orderService.createOrder(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasAnyRole('WAITER', 'MANAGER', 'ADMIN', 'OWNER')")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Principal principal) {
        List<OrderResponse> orders = orderService.getMyOrders(principal.getName());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/branches/{branchId}")
    @PreAuthorize("hasAnyRole('WAITER', 'CASHIER', 'MANAGER', 'ADMIN', 'OWNER', 'KITCHEN')")
    public ResponseEntity<List<OrderResponse>> getBranchOrders(@PathVariable Long branchId,
                                                               Principal principal) {
        List<OrderResponse> orders = orderService.getBranchOrders(branchId, principal.getName());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAITER', 'CASHIER', 'MANAGER', 'ADMIN', 'OWNER', 'KITCHEN')")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id,
                                                      Principal principal) {
        OrderResponse order = orderService.getOrderById(id, principal.getName());
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('WAITER', 'MANAGER', 'ADMIN', 'OWNER')")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id,
                                                     @RequestBody(required = false) CancelOrderRequest request,
                                                     Principal principal) {
        OrderResponse order = orderService.cancelOrder(id, request, principal.getName());
        return ResponseEntity.ok(order);
    }
}
