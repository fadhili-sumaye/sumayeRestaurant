package com.sumaye.restaurant.service;

import com.sumaye.restaurant.exception.ResourceNotFoundException;
import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.MenuCategory;
import com.sumaye.restaurant.repository.BranchRepository;
import com.sumaye.restaurant.repository.MenuCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuCategoryService {
    private final MenuCategoryRepository categoryRepository;
    private final BranchRepository branchRepository;

    public List<MenuCategory> getCategoriesByBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        return categoryRepository.findByBranchOrderByNameAsc(branch);
    }

    public MenuCategory getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    public MenuCategory createCategory(Long branchId, String name, String description) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        MenuCategory category = new MenuCategory();
        category.setBranch(branch);
        category.setName(name);
        category.setDescription(description);
        category.setActive(true);
        category.setCreatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    public MenuCategory updateCategory(Long id, String name, String description, boolean active) {
        MenuCategory category = getCategory(id);
        category.setName(name);
        category.setDescription(description);
        category.setActive(active);
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.delete(getCategory(id));
    }
}
