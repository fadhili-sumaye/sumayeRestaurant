package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.dto.DailyClosingRequest;
import com.sumaye.restaurant.dto.DailyClosingResponse;
import com.sumaye.restaurant.service.DailyClosingService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/branches/{branchId}/daily-closings")
@RequiredArgsConstructor
public class DailyClosingController {
    private final DailyClosingService service;

    @GetMapping("/{businessDate}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public DailyClosingResponse get(@PathVariable Long branchId, @PathVariable LocalDate businessDate) {
        return DailyClosingResponse.from(service.get(branchId, businessDate));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public ResponseEntity<DailyClosingResponse> close(
            @PathVariable Long branchId,
            @Valid @RequestBody DailyClosingRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DailyClosingResponse.from(service.close(branchId, request, user.getUsername())));
    }
}
