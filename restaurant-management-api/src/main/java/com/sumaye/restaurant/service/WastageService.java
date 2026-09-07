package com.sumaye.restaurant.service;

import com.sumaye.restaurant.dto.*;
import com.sumaye.restaurant.exception.ResourceNotFoundException;
import com.sumaye.restaurant.model.*;
import com.sumaye.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WastageService {

    private final WastageRecordRepository wastageRecordRepository;
    private final IngredientRepository ingredientRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public List<WastageRecordResponse> getWastageByBranch(Long branchId) {
        return wastageRecordRepository.findByBranchIdOrderByCreatedAtDesc(branchId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WastageRecordResponse recordWastage(Long branchId, RecordWastageRequest request, String username) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Tawi halikupatikana"));

        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException("Kiungo hakikupatikana"));

        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Mtumiaji hakupatikana"));
        inventoryService.assertBranchAccess(actor, branch);

        WastageRecord record = new WastageRecord();
        record.setBranch(branch);
        record.setIngredient(ingredient);
        record.setQuantity(request.getQuantity());
        record.setUnit(request.getUnit());
        record.setReason(request.getReason());
        record.setRecordedBy(actor);
        record.setWastageDate(request.getWastageDate() != null ? request.getWastageDate() : LocalDate.now());
        record.setCreatedAt(LocalDateTime.now());

        WastageRecord savedRecord = wastageRecordRepository.save(record);

        // The saved record ID is kept in the immutable inventory audit trail.
        inventoryService.recordWastage(branch, ingredient, request.getQuantity(),
                request.getUnit(), request.getReason(), actor, savedRecord.getId());

        return mapToResponse(savedRecord);
    }

    private WastageRecordResponse mapToResponse(WastageRecord r) {
        return WastageRecordResponse.builder()
                .id(r.getId())
                .branchId(r.getBranch().getId())
                .ingredientId(r.getIngredient().getId())
                .ingredientName(r.getIngredient().getName())
                .quantity(r.getQuantity())
                .unit(r.getUnit())
                .reason(r.getReason())
                .recordedByName(r.getRecordedBy() != null ? r.getRecordedBy().getUsername() : null)
                .wastageDate(r.getWastageDate())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
