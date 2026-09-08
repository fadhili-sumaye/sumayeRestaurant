package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.model.MenuCategory;
import com.sumaye.restaurant.model.MenuItem;
import com.sumaye.restaurant.repository.MenuCategoryRepository;
import com.sumaye.restaurant.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public read-only menu endpoint.
 * Allows customers to browse available food and drink items without authentication.
 * No sensitive data (users, passwords, financial, inventory) is exposed.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicMenuController {

    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;

    /**
     * GET /api/public/menu
     * Returns all available menu items across all active branches.
     * No authentication required.
     */
    @GetMapping("/menu")
    public List<MenuItem> getPublicMenu() {
        return menuItemRepository.findByAvailableOrderByNameAsc(true);
    }

    /**
     * GET /api/public/menu/categories
     * Returns all active menu categories across all branches.
     * No authentication required.
     */
    @GetMapping("/menu/categories")
    public List<MenuCategory> getPublicCategories() {
        return menuCategoryRepository.findByActiveOrderByNameAsc(true);
    }
}
