package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.dto.QrLinkResponse;
import com.sumaye.restaurant.model.RestaurantTable;
import com.sumaye.restaurant.service.QrOrderingService;
import com.sumaye.restaurant.service.RestaurantTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RestaurantTableController {
    private final RestaurantTableService tableService;
    private final QrOrderingService qrOrderingService;

    @GetMapping("/branches/{branchId}/tables")
    public List<RestaurantTable> getTables(@PathVariable Long branchId, Principal principal) {
        return tableService.getTablesByBranch(branchId, principal != null ? principal.getName() : null);
    }

    @GetMapping("/tables")
    public List<RestaurantTable> getAllTables(@RequestParam(required = false) Long branchId, Principal principal) {
        return tableService.getTablesByBranch(branchId, principal != null ? principal.getName() : null);
    }

    @GetMapping("/tables/{id}")
    public RestaurantTable getTable(@PathVariable Long id) {
        return tableService.getTable(id);
    }

    @PostMapping("/branches/{branchId}/tables")
    public RestaurantTable createTable(@PathVariable Long branchId,
                                      @RequestParam Integer tableNumber,
                                      @RequestParam Integer capacity,
                                      @RequestParam(defaultValue = "AVAILABLE") RestaurantTable.TableStatus status) {
        return tableService.createTable(branchId, tableNumber, capacity, status);
    }

    @PutMapping("/tables/{id}")
    public RestaurantTable updateTable(@PathVariable Long id,
                                      @RequestParam Integer tableNumber,
                                      @RequestParam Integer capacity,
                                      @RequestParam RestaurantTable.TableStatus status) {
        return tableService.updateTable(id, tableNumber, capacity, status);
    }

    @PatchMapping("/tables/{id}/status")
    public RestaurantTable updateTableStatus(@PathVariable Long id,
                                            @RequestParam RestaurantTable.TableStatus status) {
        return tableService.updateTableStatus(id, status);
    }

    @GetMapping("/tables/{id}/qr-link")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public QrLinkResponse getQrLink(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        return qrOrderingService.getOrCreateLink(id, user.getUsername());
    }

    @PostMapping("/tables/{id}/qr-link/rotate")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public QrLinkResponse rotateQrLink(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        return qrOrderingService.rotateLink(id, user.getUsername());
    }

    @DeleteMapping("/tables/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}
