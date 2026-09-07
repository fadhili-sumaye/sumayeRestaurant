package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.dto.*;
import com.sumaye.restaurant.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping("/menu-items/{menuItemId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER', 'KITCHEN', 'STOREKEEPER')")
    public ResponseEntity<RecipeResponse> getByMenuItemId(@PathVariable Long menuItemId) {
        return ResponseEntity.ok(recipeService.getRecipeByMenuItemId(menuItemId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<RecipeResponse> save(
            @Valid @RequestBody CreateRecipeRequest request,
            @RequestParam(required = false, defaultValue = "0") Long branchId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recipeService.saveRecipe(request, branchId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/menu-items/{menuItemId}/cost")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER', 'STOREKEEPER')")
    public ResponseEntity<RecipeResponse> getCost(
            @PathVariable Long menuItemId,
            @RequestParam Long branchId) {
        return ResponseEntity.ok(recipeService.getRecipeCost(menuItemId, branchId));
    }
}
