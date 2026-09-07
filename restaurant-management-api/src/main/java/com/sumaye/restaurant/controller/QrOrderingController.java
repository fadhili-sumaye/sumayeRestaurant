package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.dto.OrderResponse;
import com.sumaye.restaurant.dto.QrOrderRequest;
import com.sumaye.restaurant.dto.QrTableMenuResponse;
import com.sumaye.restaurant.service.QrOrderingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/qr")
@RequiredArgsConstructor
public class QrOrderingController {
    private final QrOrderingService service;

    @GetMapping("/{token}")
    public QrTableMenuResponse menu(@PathVariable String token) {
        return service.getMenu(token);
    }

    @PostMapping("/{token}/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @PathVariable String token,
            @Valid @RequestBody QrOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createOrder(token, request));
    }
}
