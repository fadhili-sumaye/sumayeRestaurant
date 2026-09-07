package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.dto.CreateStaffRequest;
import com.sumaye.restaurant.dto.StaffResponse;
import com.sumaye.restaurant.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<StaffResponse>> getStaff(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String role,
            Principal principal) {
        return ResponseEntity.ok(staffService.getStaff(branchId, role, principal.getName()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<StaffResponse> createStaff(
            @Valid @RequestBody CreateStaffRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(staffService.createStaff(request, principal.getName()));
    }

    @PutMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<StaffResponse> toggleActive(
            @PathVariable Long id,
            Principal principal) {
        return ResponseEntity.ok(staffService.toggleActive(id, principal.getName()));
    }
}
