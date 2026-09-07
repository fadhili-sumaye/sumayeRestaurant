package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BranchController {
    private final BranchService branchService;

    @GetMapping("/restaurants/{restaurantId}/branches")
    public List<Branch> getBranches(@PathVariable Long restaurantId) {
        return branchService.getBranchesByRestaurant(restaurantId);
    }

    @GetMapping("/branches/{id}")
    public Branch getBranch(@PathVariable Long id) {
        return branchService.getBranch(id);
    }

    @PostMapping("/restaurants/{restaurantId}/branches")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public Branch createBranch(@PathVariable Long restaurantId,
                               @RequestParam String name,
                               @RequestParam(required = false) String location,
                               @RequestParam(required = false) String phoneNumber,
                               @RequestParam(required = false) String address) {
        return branchService.createBranch(restaurantId, name, location, phoneNumber, address);
    }

    @PutMapping("/branches/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public Branch updateBranch(@PathVariable Long id,
                               @RequestParam String name,
                               @RequestParam(required = false) String location,
                               @RequestParam(required = false) String phoneNumber,
                               @RequestParam(required = false) String address,
                               @RequestParam(defaultValue = "true") boolean active) {
        return branchService.updateBranch(id, name, location, phoneNumber, address, active);
    }

    @PatchMapping("/branches/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public Branch toggleBranchStatus(@PathVariable Long id) {
        return branchService.toggleBranchStatus(id);
    }

    @DeleteMapping("/branches/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.noContent().build();
    }
}
