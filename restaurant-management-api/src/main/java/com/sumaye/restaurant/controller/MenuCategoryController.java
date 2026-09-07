package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.model.MenuCategory;
import com.sumaye.restaurant.service.MenuCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuCategoryController {
    private final MenuCategoryService categoryService;

    @GetMapping("/branches/{branchId}/categories")
    public List<MenuCategory> getCategories(@PathVariable Long branchId) {
        return categoryService.getCategoriesByBranch(branchId);
    }

    @GetMapping("/categories/{id}")
    public MenuCategory getCategory(@PathVariable Long id) {
        return categoryService.getCategory(id);
    }

    @PostMapping("/branches/{branchId}/categories")
    public MenuCategory createCategory(@PathVariable Long branchId,
                                      @RequestParam String name,
                                      @RequestParam(required = false) String description) {
        return categoryService.createCategory(branchId, name, description);
    }

    @PutMapping("/categories/{id}")
    public MenuCategory updateCategory(@PathVariable Long id,
                                      @RequestParam String name,
                                      @RequestParam(required = false) String description,
                                      @RequestParam(defaultValue = "true") boolean active) {
        return categoryService.updateCategory(id, name, description, active);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
