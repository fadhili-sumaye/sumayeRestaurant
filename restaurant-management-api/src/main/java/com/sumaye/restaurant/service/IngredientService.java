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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    @Transactional(readOnly = true)
    public List<IngredientResponse> getAllActiveIngredients() {
        return ingredientRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IngredientResponse createIngredient(CreateIngredientRequest request) {
        ingredientRepository.findByNameIgnoreCase(request.getName()).ifPresent(i -> {
            throw new ApiException("Kiungo chenye jina hilo tayari kipo: " + request.getName());
        });

        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.getName().trim());
        ingredient.setDescription(request.getDescription());
        ingredient.setKiswahiliName(request.getKiswahiliName());
        ingredient.setCategory(request.getCategory());
        ingredient.setDefaultUnit(request.getDefaultUnit());
        ingredient.setActive(true);
        ingredient.setCreatedAt(LocalDateTime.now());

        return mapToResponse(ingredientRepository.save(ingredient));
    }

    @Transactional
    public IngredientResponse updateIngredient(Long id, CreateIngredientRequest request) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kiungo hakikupatikana"));

        ingredient.setName(request.getName().trim());
        ingredient.setDescription(request.getDescription());
        ingredient.setKiswahiliName(request.getKiswahiliName());
        ingredient.setCategory(request.getCategory());
        ingredient.setDefaultUnit(request.getDefaultUnit());
        ingredient.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(ingredientRepository.save(ingredient));
    }

    @Transactional
    public void deactivateIngredient(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kiungo hakikupatikana"));
        ingredient.setActive(false);
        ingredient.setUpdatedAt(LocalDateTime.now());
        ingredientRepository.save(ingredient);
    }

    private IngredientResponse mapToResponse(Ingredient i) {
        return IngredientResponse.builder()
                .id(i.getId())
                .name(i.getName())
                .description(i.getDescription())
                .kiswahiliName(i.getKiswahiliName())
                .category(i.getCategory())
                .defaultUnit(i.getDefaultUnit())
                .active(i.isActive())
                .createdAt(i.getCreatedAt())
                .build();
    }
}
