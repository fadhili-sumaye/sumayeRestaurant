package com.sumaye.restaurant.service;

import com.sumaye.restaurant.exception.ResourceNotFoundException;
import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.MenuCategory;
import com.sumaye.restaurant.model.MenuItem;
import com.sumaye.restaurant.repository.BranchRepository;
import com.sumaye.restaurant.repository.MenuCategoryRepository;
import com.sumaye.restaurant.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuItemService {
    private final MenuItemRepository itemRepository;
    private final MenuCategoryRepository categoryRepository;
    private final BranchRepository branchRepository;

    public List<MenuItem> getItemsByBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        return itemRepository.findByBranchOrderByNameAsc(branch);
    }

    public MenuItem getMenuItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
    }

    public MenuItem createMenuItem(Long branchId, Long categoryId, String name, String description,
                                  BigDecimal price, String imageUrl, Integer preparationTimeMinutes, boolean available) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        MenuCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        MenuItem item = new MenuItem();
        item.setBranch(branch);
        item.setCategory(category);
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setImageUrl(imageUrl);
        item.setPreparationTimeMinutes(preparationTimeMinutes);
        item.setAvailable(available);
        item.setCreatedAt(LocalDateTime.now());
        return itemRepository.save(item);
    }

    public MenuItem updateMenuItem(Long id, Long categoryId, String name, String description,
                                  BigDecimal price, String imageUrl, Integer preparationTimeMinutes, boolean available) {
        MenuItem item = getMenuItem(id);
        if (categoryId != null) {
            MenuCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            item.setCategory(category);
        }
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setImageUrl(imageUrl);
        item.setPreparationTimeMinutes(preparationTimeMinutes);
        item.setAvailable(available);
        item.setUpdatedAt(LocalDateTime.now());
        return itemRepository.save(item);
    }

    public void deleteMenuItem(Long id) {
        itemRepository.delete(getMenuItem(id));
    }

    public MenuItem toggleAvailability(Long id) {
        MenuItem item = getMenuItem(id);
        item.setAvailable(!item.isAvailable());
        item.setUpdatedAt(LocalDateTime.now());
        return itemRepository.save(item);
    }
}
