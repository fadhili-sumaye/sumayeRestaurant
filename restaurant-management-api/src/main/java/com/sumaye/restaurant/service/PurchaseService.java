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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final IngredientRepository ingredientRepository;
    private final SupplierRepository supplierRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getPurchasesByBranch(Long branchId) {
        return purchaseRepository.findByBranchIdOrderByCreatedAtDesc(branchId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PurchaseResponse createPurchase(Long branchId, CreatePurchaseRequest request, String username) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Tawi halikupatikana"));
        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Mtumiaji hakupatikana"));
        inventoryService.assertBranchAccess(actor, branch);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ApiException("Orodha ya bidhaa inahitajika");
        }

        Purchase purchase = new Purchase();
        purchase.setBranch(branch);
        purchase.setReferenceNumber(request.getReferenceNumber());
        purchase.setPurchaseDate(request.getPurchaseDate());
        purchase.setNotes(request.getNotes());
        purchase.setStatus(Purchase.PurchaseStatus.PENDING);

        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Msambazaji hakupatikana"));
            if (!supplier.getBranch().getId().equals(branch.getId())) {
                throw new ApiException("Msambazaji huyu hajasajiliwa kwenye tawi hili");
            }
            purchase.setSupplier(supplier);
        }

        BigDecimal totalCost = BigDecimal.ZERO;
        for (CreatePurchaseRequest.PurchaseItemRequest itemReq : request.getItems()) {
            Ingredient ingredient = ingredientRepository.findById(itemReq.getIngredientId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Kiungo hakikupatikana: ID " + itemReq.getIngredientId()));

            PurchaseItem pi = new PurchaseItem();
            pi.setPurchase(purchase);
            pi.setIngredient(ingredient);
            pi.setQuantityOrdered(itemReq.getQuantityOrdered());
            pi.setQuantityReceived(BigDecimal.ZERO);
            pi.setUnit(itemReq.getUnit() != null ? itemReq.getUnit() : ingredient.getDefaultUnit());
            BigDecimal unitCost = itemReq.getUnitCost() != null ? itemReq.getUnitCost() : BigDecimal.ZERO;
            pi.setUnitCost(unitCost);
            pi.setTotalCost(unitCost.multiply(itemReq.getQuantityOrdered()));
            totalCost = totalCost.add(pi.getTotalCost());
            purchase.getItems().add(pi);
        }

        purchase.setTotalCost(totalCost);
        Purchase saved = purchaseRepository.save(purchase);
        return mapToResponse(saved);
    }

    @Transactional
    public PurchaseResponse receivePurchase(Long purchaseId, ReceivePurchaseRequest request, String username) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Ununuzi haukupatikana"));

        if (purchase.getStatus() != Purchase.PurchaseStatus.PENDING) {
            throw new ApiException("Ununuzi huu tayari umepokelewa au umeghairiwa");
        }

        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Mtumiaji hakupatikana"));
        inventoryService.assertBranchAccess(actor, purchase.getBranch());

        // Build map of purchaseItemId → quantityReceived
        Map<Long, BigDecimal> receivedMap = request.getItems().stream()
                .collect(Collectors.toMap(
                        ReceivePurchaseRequest.ReceivedItem::getPurchaseItemId,
                        ReceivePurchaseRequest.ReceivedItem::getQuantityReceived
                ));

        for (PurchaseItem pi : purchase.getItems()) {
            BigDecimal qtyReceived = receivedMap.getOrDefault(pi.getId(), BigDecimal.ZERO);
            pi.setQuantityReceived(qtyReceived);

            if (qtyReceived.compareTo(BigDecimal.ZERO) > 0) {
                inventoryService.creditStockForPurchaseItem(purchase.getBranch(), pi, actor);
            }
        }

        purchase.setStatus(Purchase.PurchaseStatus.RECEIVED);
        purchase.setReceivedBy(actor);
        purchase.setReceivedAt(LocalDateTime.now());
        purchase.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(purchaseRepository.save(purchase));
    }

    @Transactional
    public PurchaseResponse cancelPurchase(Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Ununuzi haukupatikana"));

        if (purchase.getStatus() == Purchase.PurchaseStatus.RECEIVED) {
            throw new ApiException("Huwezi kughairi ununuzi uliokwisha pokelewa");
        }

        purchase.setStatus(Purchase.PurchaseStatus.CANCELLED);
        purchase.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(purchaseRepository.save(purchase));
    }

    private PurchaseResponse mapToResponse(Purchase p) {
        List<PurchaseResponse.PurchaseItemResponse> items = p.getItems().stream().map(pi ->
                PurchaseResponse.PurchaseItemResponse.builder()
                        .id(pi.getId())
                        .ingredientId(pi.getIngredient().getId())
                        .ingredientName(pi.getIngredient().getName())
                        .quantityOrdered(pi.getQuantityOrdered())
                        .quantityReceived(pi.getQuantityReceived())
                        .unit(pi.getUnit())
                        .unitCost(pi.getUnitCost())
                        .totalCost(pi.getTotalCost())
                        .build()
        ).collect(Collectors.toList());

        return PurchaseResponse.builder()
                .id(p.getId())
                .branchId(p.getBranch().getId())
                .branchName(p.getBranch().getName())
                .supplierId(p.getSupplier() != null ? p.getSupplier().getId() : null)
                .supplierName(p.getSupplier() != null ? p.getSupplier().getName() : null)
                .referenceNumber(p.getReferenceNumber())
                .purchaseDate(p.getPurchaseDate())
                .totalCost(p.getTotalCost())
                .status(p.getStatus().name())
                .notes(p.getNotes())
                .receivedByName(p.getReceivedBy() != null ? p.getReceivedBy().getUsername() : null)
                .receivedAt(p.getReceivedAt())
                .items(items)
                .createdAt(p.getCreatedAt())
                .build();
    }
}
