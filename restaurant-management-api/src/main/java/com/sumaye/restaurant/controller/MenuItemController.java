package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.model.MenuItem;
import com.sumaye.restaurant.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuItemController {
    private final MenuItemService menuItemService;

    @GetMapping("/branches/{branchId}/menu-items")
    public List<MenuItem> getMenuItems(@PathVariable Long branchId) {
        return menuItemService.getItemsByBranch(branchId);
    }

    @GetMapping("/menu-items/{id}")
    public MenuItem getMenuItem(@PathVariable Long id) {
        return menuItemService.getMenuItem(id);
    }

    @PostMapping("/branches/{branchId}/menu-items")
    public MenuItem createMenuItem(@PathVariable Long branchId,
                                  @RequestParam Long categoryId,
                                  @RequestParam String name,
                                  @RequestParam String description,
                                  @RequestParam BigDecimal price,
                                  @RequestParam(required = false) String imageUrl,
                                  @RequestParam(required = false) Integer preparationTimeMinutes,
                                  @RequestParam(defaultValue = "true") boolean available) {
        return menuItemService.createMenuItem(branchId, categoryId, name, description, price, imageUrl,
                preparationTimeMinutes, available);
    }

    @PutMapping("/menu-items/{id}")
    public MenuItem updateMenuItem(@PathVariable Long id,
                                  @RequestParam Long categoryId,
                                  @RequestParam String name,
                                  @RequestParam String description,
                                  @RequestParam BigDecimal price,
                                  @RequestParam(required = false) String imageUrl,
                                  @RequestParam(required = false) Integer preparationTimeMinutes,
                                  @RequestParam(defaultValue = "true") boolean available) {
        return menuItemService.updateMenuItem(id, categoryId, name, description, price, imageUrl,
                preparationTimeMinutes, available);
    }

    @PatchMapping("/menu-items/{id}/toggle-availability")
    public MenuItem toggleAvailability(@PathVariable Long id) {
        return menuItemService.toggleAvailability(id);
    }

    @DeleteMapping("/menu-items/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
}
