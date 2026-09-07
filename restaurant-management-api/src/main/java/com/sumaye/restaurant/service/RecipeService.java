package com.sumaye.restaurant.service;

import com.sumaye.restaurant.dto.*;
import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.exception.ResourceNotFoundException;
import com.sumaye.restaurant.model.*;
import com.sumaye.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeItemRepository recipeItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final IngredientRepository ingredientRepository;
    private final InventoryStockRepository stockRepository;

    @Transactional(readOnly = true)
    public RecipeResponse getRecipeByMenuItemId(Long menuItemId) {
        Recipe recipe = recipeRepository.findByMenuItemId(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mapishi hayajaandaliwa kwa chakula hiki"));
        return mapToResponse(recipe);
    }

    @Transactional
    public RecipeResponse saveRecipe(CreateRecipeRequest request, Long branchId) {
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Chakula hakikupatikana"));

        // Upsert: if recipe exists, replace items
        Recipe recipe = recipeRepository.findByMenuItemId(request.getMenuItemId())
                .orElseGet(Recipe::new);

        recipe.setMenuItem(menuItem);
        recipe.setName(request.getName());
        recipe.setActive(true);
        recipe.setUpdatedAt(LocalDateTime.now());
        if (recipe.getCreatedAt() == null) recipe.setCreatedAt(LocalDateTime.now());

        // Clear old items
        recipe.getItems().clear();

        for (CreateRecipeRequest.RecipeItemRequest itemReq : request.getItems()) {
            Ingredient ingredient = ingredientRepository.findById(itemReq.getIngredientId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Kiungo hakikupatikana: ID " + itemReq.getIngredientId()));

            RecipeItem ri = new RecipeItem();
            ri.setRecipe(recipe);
            ri.setIngredient(ingredient);
            ri.setQuantityRequired(itemReq.getQuantityRequired());
            ri.setUnit(itemReq.getUnit());
            recipe.getItems().add(ri);
        }

        Recipe saved = recipeRepository.save(recipe);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mapishi hayakupatikana"));
        recipe.setActive(false);
        recipe.setUpdatedAt(LocalDateTime.now());
        recipeRepository.save(recipe);
    }

    @Transactional(readOnly = true)
    public RecipeResponse getRecipeCost(Long menuItemId, Long branchId) {
        Recipe recipe = recipeRepository.findByMenuItemId(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mapishi hayajaandaliwa kwa chakula hiki"));

        // Build response with cost contributions from branch stock
        RecipeResponse response = mapToResponse(recipe);
        BigDecimal totalCost = BigDecimal.ZERO;

        List<RecipeResponse.RecipeItemResponse> enrichedItems = recipe.getItems().stream().map(ri -> {
            BigDecimal costPerUnit = stockRepository
                    .findByBranchIdAndIngredientId(branchId, ri.getIngredient().getId())
                    .map(s -> s.getCostPerUnit() != null ? s.getCostPerUnit() : BigDecimal.ZERO)
                    .orElse(BigDecimal.ZERO);

            BigDecimal quantityInStockUnit = convertUnits(ri.getQuantityRequired(), ri.getUnit(),
                    stockRepository.findByBranchIdAndIngredientId(branchId, ri.getIngredient().getId())
                            .map(InventoryStock::getUnit).orElse(ri.getUnit()));
            BigDecimal cost = costPerUnit.multiply(quantityInStockUnit)
                    .setScale(2, RoundingMode.HALF_UP);

            return RecipeResponse.RecipeItemResponse.builder()
                    .id(ri.getId())
                    .ingredientId(ri.getIngredient().getId())
                    .ingredientName(ri.getIngredient().getName())
                    .quantityRequired(ri.getQuantityRequired())
                    .unit(ri.getUnit())
                    .costContribution(cost)
                    .build();
        }).collect(Collectors.toList());

        BigDecimal estimatedCost = enrichedItems.stream()
                .map(i -> i.getCostContribution() != null ? i.getCostContribution() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setItems(enrichedItems);
        response.setEstimatedCost(estimatedCost);
        return response;
    }

    private RecipeResponse mapToResponse(Recipe recipe) {
        List<RecipeResponse.RecipeItemResponse> items = recipe.getItems().stream().map(ri ->
                RecipeResponse.RecipeItemResponse.builder()
                        .id(ri.getId())
                        .ingredientId(ri.getIngredient().getId())
                        .ingredientName(ri.getIngredient().getName())
                        .quantityRequired(ri.getQuantityRequired())
                        .unit(ri.getUnit())
                        .build()
        ).collect(Collectors.toList());

        return RecipeResponse.builder()
                .id(recipe.getId())
                .menuItemId(recipe.getMenuItem().getId())
                .menuItemName(recipe.getMenuItem().getName())
                .name(recipe.getName())
                .active(recipe.isActive())
                .items(items)
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .build();
    }

    private BigDecimal convertUnits(BigDecimal quantity, Ingredient.IngredientUnit from,
                                    Ingredient.IngredientUnit to) {
        if (from == to) return quantity;
        if (from == Ingredient.IngredientUnit.KG && to == Ingredient.IngredientUnit.GRAM
                || from == Ingredient.IngredientUnit.LITRE && to == Ingredient.IngredientUnit.ML) {
            return quantity.multiply(BigDecimal.valueOf(1000));
        }
        if (from == Ingredient.IngredientUnit.GRAM && to == Ingredient.IngredientUnit.KG
                || from == Ingredient.IngredientUnit.ML && to == Ingredient.IngredientUnit.LITRE) {
            return quantity.divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
        }
        throw new ApiException("Vipimo vya mapishi haviendani: " + from + " na " + to);
    }
}
