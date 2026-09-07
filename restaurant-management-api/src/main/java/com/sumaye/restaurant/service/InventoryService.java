package com.sumaye.restaurant.service;

import com.sumaye.restaurant.dto.*;
import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.exception.ResourceNotFoundException;
import com.sumaye.restaurant.model.*;
import com.sumaye.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryStockRepository stockRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final OrderRepository orderRepository;

    // ──────────────────────────────────────────────────────────────────────────
    // STOCK QUERIES
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<InventoryStockResponse> getStockForBranch(Long branchId) {
        Branch branch = getBranch(branchId);
        return stockRepository.findByBranch(branch).stream()
                .map(this::mapToStockResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LowStockSummaryResponse getLowStockSummary(Long branchId) {
        Branch branch = getBranch(branchId);
        List<InventoryStock> lowItems = stockRepository.findLowStockByBranchId(branchId);
        List<InventoryStockResponse> responses = lowItems.stream()
                .map(this::mapToStockResponse)
                .collect(Collectors.toList());

        long outOfStock = lowItems.stream()
                .filter(s -> s.getQuantityOnHand().compareTo(BigDecimal.ZERO) <= 0)
                .count();

        return LowStockSummaryResponse.builder()
                .branchId(branchId)
                .branchName(branch.getName())
                .totalLowStockItems(lowItems.size())
                .outOfStockItems((int) outOfStock)
                .lowStockItems(responses)
                .build();
    }

    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> getTransactions(Long branchId) {
        return transactionRepository.findByBranchIdOrderByCreatedAtDesc(branchId).stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // STOCK DEDUCTION — triggered when order is sent to kitchen
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional
    public void deductStockForOrder(Order order) {
        // Idempotency guard — never deduct twice for same order
        if (order.isInventoryDeducted()) {
            log.warn("Stock already deducted for order {}. Skipping.", order.getOrderNumber());
            return;
        }

        Branch branch = order.getBranch();
        boolean anyLowStock = false;

        for (OrderItem orderItem : order.getItems()) {
            Optional<Recipe> recipeOpt = recipeRepository.findByMenuItemId(
                    orderItem.getMenuItem().getId());

            if (recipeOpt.isEmpty()) {
                // No recipe configured — skip gracefully (log warning, don't block)
                log.warn("No recipe for menu item '{}' (id={}). Stock not deducted.",
                        orderItem.getItemName(), orderItem.getMenuItem().getId());
                continue;
            }

            Recipe recipe = recipeOpt.get();
            int qty = orderItem.getQuantity();

            for (RecipeItem ri : recipe.getItems()) {
                BigDecimal totalRequired = ri.getQuantityRequired()
                        .multiply(BigDecimal.valueOf(qty));

                // Convert to the stock's stored unit before deducting
                Optional<InventoryStock> stockOpt = stockRepository
                        .findByBranchAndIngredient(branch, ri.getIngredient());

                if (stockOpt.isEmpty()) {
                    // Stock not set up for this branch — log and continue (lenient)
                    log.warn("No stock record for ingredient '{}' in branch '{}'. Skipping deduction.",
                            ri.getIngredient().getName(), branch.getName());
                    continue;
                }

                InventoryStock stock = stockOpt.get();
                BigDecimal deductionInStockUnit = convertUnits(
                        totalRequired, ri.getUnit(), stock.getUnit());

                BigDecimal previousQty = stock.getQuantityOnHand();
                BigDecimal newQty = previousQty.subtract(deductionInStockUnit);

                if (newQty.compareTo(BigDecimal.ZERO) < 0) {
                    throw new ApiException("Hisa ya " + ri.getIngredient().getName()
                            + " haitoshi kwenye tawi la " + branch.getName());
                }

                stock.setQuantityOnHand(newQty);
                stock.setUpdatedAt(LocalDateTime.now());
                stockRepository.save(stock);

                // Create audit transaction
                InventoryTransaction tx = new InventoryTransaction();
                tx.setBranch(branch);
                tx.setIngredient(ri.getIngredient());
                tx.setTransactionType(InventoryTransaction.TransactionType.SALE_CONSUMPTION);
                tx.setQuantityChange(deductionInStockUnit.negate());
                tx.setQuantityBefore(previousQty);
                tx.setQuantityAfter(newQty);
                tx.setUnit(stock.getUnit());
                tx.setReferenceType("ORDER");
                tx.setReferenceId(order.getId());
                tx.setNotes("Oda #" + order.getOrderNumber());
                tx.setCreatedAt(LocalDateTime.now());
                transactionRepository.save(tx);

                // Check low-stock threshold
                if (newQty.compareTo(stock.getMinimumStockLevel()) <= 0) {
                    anyLowStock = true;
                    publishLowStockAlert(branch.getId(), stock, newQty);
                }
            }
        }

        // Mark order as deducted to prevent future duplicates
        order.setInventoryDeducted(true);
        orderRepository.save(order);

        log.info("Stock deducted for order {}", order.getOrderNumber());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // STOCK REVERSAL — triggered when order is cancelled before preparation
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional
    public void reverseStockForOrder(Order order) {
        if (!order.isInventoryDeducted()) {
            log.info("Order {} had no stock deduction — no reversal needed.", order.getOrderNumber());
            return;
        }

        Branch branch = order.getBranch();

        for (OrderItem orderItem : order.getItems()) {
            Optional<Recipe> recipeOpt = recipeRepository.findByMenuItemId(
                    orderItem.getMenuItem().getId());
            if (recipeOpt.isEmpty()) continue;

            Recipe recipe = recipeOpt.get();
            int qty = orderItem.getQuantity();

            for (RecipeItem ri : recipe.getItems()) {
                BigDecimal totalToReturn = ri.getQuantityRequired()
                        .multiply(BigDecimal.valueOf(qty));

                Optional<InventoryStock> stockOpt = stockRepository
                        .findByBranchAndIngredient(branch, ri.getIngredient());
                if (stockOpt.isEmpty()) continue;

                InventoryStock stock = stockOpt.get();
                BigDecimal returnInStockUnit = convertUnits(
                        totalToReturn, ri.getUnit(), stock.getUnit());

                BigDecimal previousQty = stock.getQuantityOnHand();
                BigDecimal newQty = previousQty.add(returnInStockUnit);
                stock.setQuantityOnHand(newQty);
                stock.setUpdatedAt(LocalDateTime.now());
                stockRepository.save(stock);

                InventoryTransaction tx = new InventoryTransaction();
                tx.setBranch(branch);
                tx.setIngredient(ri.getIngredient());
                tx.setTransactionType(InventoryTransaction.TransactionType.REVERSAL);
                tx.setQuantityChange(returnInStockUnit);
                tx.setQuantityBefore(previousQty);
                tx.setQuantityAfter(newQty);
                tx.setUnit(stock.getUnit());
                tx.setReferenceType("ORDER");
                tx.setReferenceId(order.getId());
                tx.setNotes("Kurudisha hisa — Oda #" + order.getOrderNumber() + " ilighairiwa");
                tx.setCreatedAt(LocalDateTime.now());
                transactionRepository.save(tx);
            }
        }

        order.setInventoryDeducted(false);
        orderRepository.save(order);
        log.info("Stock reversed for cancelled order {}", order.getOrderNumber());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // STOCK ADJUSTMENT (manual IN / OUT by manager)
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional
    public InventoryStockResponse adjustStock(Long branchId, StockAdjustmentRequest req, String username) {
        Branch branch = getBranch(branchId);
        Ingredient ingredient = ingredientRepository.findById(req.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException("Kiungo hakikupatikana"));

        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Mtumiaji hakupatikana"));
        assertBranchAccess(actor, branch);

        InventoryStock stock = stockRepository.findByBranchAndIngredient(branch, ingredient)
                .orElseGet(() -> {
                    InventoryStock ns = new InventoryStock();
                    ns.setBranch(branch);
                    ns.setIngredient(ingredient);
                    ns.setUnit(ingredient.getDefaultUnit());
                    return ns;
                });

        BigDecimal convertedQty = convertUnits(req.getQuantity(), req.getUnit(), stock.getUnit());
        BigDecimal previousQty = stock.getQuantityOnHand();
        BigDecimal newQty;
        InventoryTransaction.TransactionType txType;

        if (req.getAdjustmentType() == StockAdjustmentRequest.AdjustmentType.IN) {
            newQty = stock.getQuantityOnHand().add(convertedQty);
            txType = InventoryTransaction.TransactionType.ADJUSTMENT_IN;
            stock.setLastRestockedAt(LocalDateTime.now());
        } else {
            newQty = stock.getQuantityOnHand().subtract(convertedQty);
            txType = InventoryTransaction.TransactionType.ADJUSTMENT_OUT;
            if (newQty.compareTo(BigDecimal.ZERO) < 0) {
                throw new ApiException("Huwezi kutoa zaidi ya hisa iliyopo. Hisa iliyopo: "
                        + stock.getQuantityOnHand() + " " + stock.getUnit());
            }
        }

        stock.setQuantityOnHand(newQty);
        stock.setUpdatedAt(LocalDateTime.now());
        stockRepository.save(stock);

        InventoryTransaction tx = new InventoryTransaction();
        tx.setBranch(branch);
        tx.setIngredient(ingredient);
        tx.setTransactionType(txType);
        tx.setQuantityChange(req.getAdjustmentType() == StockAdjustmentRequest.AdjustmentType.IN
                ? convertedQty : convertedQty.negate());
        tx.setQuantityBefore(previousQty);
        tx.setQuantityAfter(newQty);
        tx.setUnit(stock.getUnit());
        tx.setReferenceType("ADJUSTMENT");
        tx.setNotes(req.getReason());
        tx.setCreatedBy(actor);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);

        if (newQty.compareTo(stock.getMinimumStockLevel()) <= 0) {
            publishLowStockAlert(branchId, stock, newQty);
        }

        return mapToStockResponse(stock);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // WASTAGE
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional
    public void recordWastage(Branch branch, Ingredient ingredient,
                               BigDecimal quantity, Ingredient.IngredientUnit unit,
                               String reason, User actor, Long wastageRecordId) {
        InventoryStock stock = stockRepository.findByBranchAndIngredient(branch, ingredient)
                .orElseThrow(() -> new ApiException("Hisa ya kiungo hiki haipo kwenye tawi hili"));

        BigDecimal convertedQty = convertUnits(quantity, unit, stock.getUnit());
        BigDecimal previousQty = stock.getQuantityOnHand();
        BigDecimal newQty = previousQty.subtract(convertedQty);
        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException("Kiasi cha upotevu ni kikubwa kuliko hisa iliyopo");
        }

        stock.setQuantityOnHand(newQty);
        stock.setUpdatedAt(LocalDateTime.now());
        stockRepository.save(stock);

        // Transaction log
        InventoryTransaction tx = new InventoryTransaction();
        tx.setBranch(branch);
        tx.setIngredient(ingredient);
        tx.setTransactionType(InventoryTransaction.TransactionType.WASTAGE);
        tx.setQuantityChange(convertedQty.negate());
        tx.setQuantityBefore(previousQty);
        tx.setQuantityAfter(newQty);
        tx.setUnit(stock.getUnit());
        tx.setReferenceType("WASTAGE");
        tx.setReferenceId(wastageRecordId);
        tx.setNotes(reason);
        tx.setCreatedBy(actor);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);

        if (newQty.compareTo(stock.getMinimumStockLevel()) <= 0) {
            publishLowStockAlert(branch.getId(), stock, newQty);
        }

    }

    /** Compatibility overload for internal callers that do not yet have a wastage record ID. */
    @Transactional
    public void recordWastage(Branch branch, Ingredient ingredient,
                               BigDecimal quantity, Ingredient.IngredientUnit unit,
                               String reason, User actor) {
        recordWastage(branch, ingredient, quantity, unit, reason, actor, null);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // RECEIVE PURCHASE — increases stock
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional
    public void creditStockForPurchaseItem(Branch branch, PurchaseItem pi, User actor) {
        InventoryStock stock = stockRepository.findByBranchAndIngredient(branch, pi.getIngredient())
                .orElseGet(() -> {
                    InventoryStock ns = new InventoryStock();
                    ns.setBranch(branch);
                    ns.setIngredient(pi.getIngredient());
                    ns.setUnit(pi.getUnit());
                    return ns;
                });

        BigDecimal convertedQty = convertUnits(pi.getQuantityReceived(), pi.getUnit(), stock.getUnit());
        BigDecimal previousQty = stock.getQuantityOnHand();
        BigDecimal newQty = previousQty.add(convertedQty);
        stock.setQuantityOnHand(newQty);
        stock.setLastRestockedAt(LocalDateTime.now());
        if (pi.getUnitCost() != null && pi.getUnitCost().compareTo(BigDecimal.ZERO) > 0) {
            stock.setCostPerUnit(pi.getUnitCost());
        }
        stock.setUpdatedAt(LocalDateTime.now());
        stockRepository.save(stock);

        InventoryTransaction tx = new InventoryTransaction();
        tx.setBranch(branch);
        tx.setIngredient(pi.getIngredient());
        tx.setTransactionType(InventoryTransaction.TransactionType.PURCHASE);
        tx.setQuantityChange(convertedQty);
        tx.setQuantityBefore(previousQty);
        tx.setQuantityAfter(newQty);
        tx.setUnit(stock.getUnit());
        tx.setReferenceType("PURCHASE");
        tx.setReferenceId(pi.getPurchase().getId());
        tx.setNotes("Uwasilishaji wa bidhaa kutoka kwa msambazaji");
        tx.setCreatedBy(actor);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // UNIT CONVERSION
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Converts quantity from sourceUnit to targetUnit.
     * Supports: KG ↔ GRAM, LITRE ↔ ML, and same-unit pass-through.
     */
    public BigDecimal convertUnits(BigDecimal qty, Ingredient.IngredientUnit from,
                                    Ingredient.IngredientUnit to) {
        if (from == to) return qty;

        // Mass conversions
        if (from == Ingredient.IngredientUnit.KG && to == Ingredient.IngredientUnit.GRAM) {
            return qty.multiply(BigDecimal.valueOf(1000));
        }
        if (from == Ingredient.IngredientUnit.GRAM && to == Ingredient.IngredientUnit.KG) {
            return qty.divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
        }

        // Volume conversions
        if (from == Ingredient.IngredientUnit.LITRE && to == Ingredient.IngredientUnit.ML) {
            return qty.multiply(BigDecimal.valueOf(1000));
        }
        if (from == Ingredient.IngredientUnit.ML && to == Ingredient.IngredientUnit.LITRE) {
            return qty.divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
        }

        throw new ApiException("Vipimo hivi haviwezi kubadilishwa: " + from + " kwenda " + to);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // WEBSOCKET ALERT
    // ──────────────────────────────────────────────────────────────────────────

    private void publishLowStockAlert(Long branchId, InventoryStock stock, BigDecimal currentQty) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("type", "LOW_STOCK");
        alert.put("branchId", branchId);
        alert.put("ingredientId", stock.getIngredient().getId());
        alert.put("ingredientName", stock.getIngredient().getName());
        alert.put("currentQty", currentQty);
        alert.put("minimumQty", stock.getMinimumStockLevel());
        alert.put("unit", stock.getUnit());
        alert.put("stockBadge", currentQty.compareTo(BigDecimal.ZERO) <= 0 ? "IMEISHA" : "STOCK NDOGO");

        String topic = "/topic/branches/" + branchId + "/inventory/alerts";
        messagingTemplate.convertAndSend(topic, alert);
        log.info("Low stock alert sent for ingredient '{}' in branch {}", stock.getIngredient().getName(), branchId);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // MAPPERS
    // ──────────────────────────────────────────────────────────────────────────

    public InventoryStockResponse mapToStockResponse(InventoryStock stock) {
        BigDecimal qty = stock.getQuantityOnHand();
        String badge;
        String color;

        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            badge = "IMEISHA";
            color = "RED";
        } else if (qty.compareTo(stock.getMinimumStockLevel()) <= 0) {
            badge = "STOCK NDOGO";
            color = "YELLOW";
        } else {
            badge = "IPO";
            color = "GREEN";
        }

        return InventoryStockResponse.builder()
                .id(stock.getId())
                .branchId(stock.getBranch().getId())
                .branchName(stock.getBranch().getName())
                .ingredientId(stock.getIngredient().getId())
                .ingredientName(stock.getIngredient().getName())
                .quantityOnHand(qty)
                .unit(stock.getUnit())
                .minimumStockLevel(stock.getMinimumStockLevel())
                .costPerUnit(stock.getCostPerUnit())
                .lastRestockedAt(stock.getLastRestockedAt())
                .stockBadge(badge)
                .badgeColor(color)
                .build();
    }

    private InventoryTransactionResponse mapToTransactionResponse(InventoryTransaction tx) {
        return InventoryTransactionResponse.builder()
                .id(tx.getId())
                .branchId(tx.getBranch().getId())
                .ingredientId(tx.getIngredient().getId())
                .ingredientName(tx.getIngredient().getName())
                .transactionType(tx.getTransactionType().name())
                .quantityChange(tx.getQuantityChange())
                .quantityBefore(tx.getQuantityBefore())
                .quantityAfter(tx.getQuantityAfter())
                .unit(tx.getUnit())
                .referenceType(tx.getReferenceType())
                .referenceId(tx.getReferenceId())
                .notes(tx.getNotes())
                .createdByName(tx.getCreatedBy() != null ? tx.getCreatedBy().getUsername() : "mfumo")
                .createdAt(tx.getCreatedAt())
                .build();
    }

    private Branch getBranch(Long branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Tawi halikupatikana"));
    }

    /** Enforces branch isolation server-side; managers remain limited to their branch. */
    public void assertBranchAccess(User user, Branch branch) {
        boolean ownerOrAdmin = user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(role -> "ROLE_OWNER".equals(role) || "ROLE_ADMIN".equals(role));
        if (ownerOrAdmin) {
            if (user.getRestaurant() == null || branch.getRestaurant() == null
                    || !user.getRestaurant().getId().equals(branch.getRestaurant().getId())) {
                throw new ApiException("Huruhusiwi kufikia tawi la mgahawa mwingine");
            }
            return;
        }
        if (user.getBranch() == null || !user.getBranch().getId().equals(branch.getId())) {
            throw new ApiException("Huruhusiwi kufikia hisa za tawi hili");
        }
    }
}
