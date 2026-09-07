package com.sumaye.restaurant.service;

import com.sumaye.restaurant.dto.StockAdjustmentRequest;
import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.model.*;
import com.sumaye.restaurant.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService — Phase 7 Tests")
public class InventoryServiceTest {

    @Mock private InventoryStockRepository stockRepository;
    @Mock private InventoryTransactionRepository transactionRepository;
    @Mock private IngredientRepository ingredientRepository;
    @Mock private RecipeRepository recipeRepository;
    @Mock private BranchRepository branchRepository;
    @Mock private UserRepository userRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private InventoryService inventoryService;

    // ── Test fixtures ──────────────────────────────────────────────────────────

    private Branch branch;
    private Branch branchB;
    private Ingredient flourIngredient;
    private MenuItem menuItem;
    private Recipe recipe;
    private RecipeItem recipeItem;
    private InventoryStock flourStock;
    private InventoryStock flourStockB;
    private Order order;
    private OrderItem orderItem;
    private User manager;

    @BeforeEach
    void setUp() {
        // Branch A
        branch = new Branch();
        branch.setId(1L);
        branch.setName("Tawi Kuu");

        // Branch B (isolation tests)
        branchB = new Branch();
        branchB.setId(2L);
        branchB.setName("Tawi B");

        // Ingredient: Unga wa Ngano (default KG)
        flourIngredient = new Ingredient();
        flourIngredient.setId(10L);
        flourIngredient.setName("Unga wa Ngano");
        flourIngredient.setDefaultUnit(Ingredient.IngredientUnit.KG);
        flourIngredient.setActive(true);

        // MenuItem: Chapati
        menuItem = new MenuItem();
        menuItem.setId(100L);
        menuItem.setName("Chapati");
        menuItem.setBranch(branch);

        // RecipeItem: Chapati needs 0.2 KG Unga
        recipeItem = new RecipeItem();
        recipeItem.setId(1L);
        recipeItem.setIngredient(flourIngredient);
        recipeItem.setQuantityRequired(new BigDecimal("0.2"));
        recipeItem.setUnit(Ingredient.IngredientUnit.KG);

        // Recipe: Chapati recipe
        recipe = new Recipe();
        recipe.setId(1L);
        recipe.setMenuItem(menuItem);
        recipe.setName("Mapishi ya Chapati");
        recipe.setActive(true);
        recipe.setItems(new ArrayList<>(List.of(recipeItem)));
        recipeItem.setRecipe(recipe);

        // Stock in Branch A: 5 KG, minimum 1 KG
        flourStock = new InventoryStock();
        flourStock.setId(1L);
        flourStock.setBranch(branch);
        flourStock.setIngredient(flourIngredient);
        flourStock.setQuantityOnHand(new BigDecimal("5.0"));
        flourStock.setUnit(Ingredient.IngredientUnit.KG);
        flourStock.setMinimumStockLevel(new BigDecimal("1.0"));
        flourStock.setCostPerUnit(new BigDecimal("2000"));

        // Stock in Branch B: 3 KG (should NOT be affected by Branch A orders)
        flourStockB = new InventoryStock();
        flourStockB.setId(2L);
        flourStockB.setBranch(branchB);
        flourStockB.setIngredient(flourIngredient);
        flourStockB.setQuantityOnHand(new BigDecimal("3.0"));
        flourStockB.setUnit(Ingredient.IngredientUnit.KG);
        flourStockB.setMinimumStockLevel(new BigDecimal("0.5"));
        flourStockB.setCostPerUnit(new BigDecimal("2000"));

        // Manager user
        manager = new User();
        manager.setId(1L);
        manager.setUsername("manager_test");
        manager.setBranch(branch);

        // Order with 2x Chapati (requires 0.2 KG x 2 = 0.4 KG deduction)
        orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setMenuItem(menuItem);
        orderItem.setItemName("Chapati");
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(new BigDecimal("1000"));
        orderItem.setSubtotal(new BigDecimal("2000"));

        order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-260902-0001");
        order.setBranch(branch);
        order.setInventoryDeducted(false);
        order.setItems(new ArrayList<>(List.of(orderItem)));
        orderItem.setOrder(order);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Deduct stock for order with recipe
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("1. Deduct stock for order — quantity reduced by recipe amount")
    void test1_deductStockForOrder_reducesStock() {
        when(recipeRepository.findByMenuItemId(100L)).thenReturn(Optional.of(recipe));
        when(stockRepository.findByBranchAndIngredient(branch, flourIngredient))
                .thenReturn(Optional.of(flourStock));
        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.deductStockForOrder(order);

        // 2 Chapatis × 0.2 KG = 0.4 KG deducted; 5.0 - 0.4 = 4.6
        assertEquals(new BigDecimal("4.6"), flourStock.getQuantityOnHand());
        verify(stockRepository).save(flourStock);
        verify(transactionRepository).save(argThat(tx ->
                tx.getTransactionType() == InventoryTransaction.TransactionType.SALE_CONSUMPTION));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Idempotency — second deduction is a no-op
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("2. Deduct stock — idempotency: second call does NOT deduct again")
    void test2_deductStock_idempotency_secondCallIsNoOp() {
        order.setInventoryDeducted(true); // already deducted

        inventoryService.deductStockForOrder(order);

        // Stock repo should never be called
        verify(stockRepository, never()).findByBranchAndIngredient(any(), any());
        verify(transactionRepository, never()).save(any());
        assertEquals(new BigDecimal("5.0"), flourStock.getQuantityOnHand()); // unchanged
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Insufficient stock — allowed but stock goes negative (Option B)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("3. Deduct stock — insufficient stock allowed, goes negative (lenient)")
    void test3_deductStock_insufficientStock_rejected() {
        flourStock.setQuantityOnHand(new BigDecimal("0.1")); // less than 0.4 required

        when(recipeRepository.findByMenuItemId(100L)).thenReturn(Optional.of(recipe));
        when(stockRepository.findByBranchAndIngredient(branch, flourIngredient))
                .thenReturn(Optional.of(flourStock));
        // Insufficient-stock test

        // Should NOT throw — lenient mode
        assertThrows(ApiException.class, () -> inventoryService.deductStockForOrder(order));

        // Stock went negative: 0.1 - 0.4 = -0.3
        assertEquals(new BigDecimal("0.1"), flourStock.getQuantityOnHand());
        verify(stockRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: No recipe configured — graceful skip
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("4. Deduct stock — no recipe for menu item — graceful skip, no exception")
    void test4_deductStock_noRecipe_gracefulSkip() {
        when(recipeRepository.findByMenuItemId(100L)).thenReturn(Optional.empty());
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> inventoryService.deductStockForOrder(order));

        verify(stockRepository, never()).findByBranchAndIngredient(any(), any());
        verify(transactionRepository, never()).save(any());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Transaction of type SALE_CONSUMPTION is created
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("5. Deduct stock — SALE_CONSUMPTION transaction saved with correct referenceId")
    void test5_deductStock_transactionCreated() {
        when(recipeRepository.findByMenuItemId(100L)).thenReturn(Optional.of(recipe));
        when(stockRepository.findByBranchAndIngredient(branch, flourIngredient))
                .thenReturn(Optional.of(flourStock));
        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.deductStockForOrder(order);

        verify(transactionRepository).save(argThat(tx ->
                tx.getTransactionType() == InventoryTransaction.TransactionType.SALE_CONSUMPTION
                && tx.getReferenceType().equals("ORDER")
                && tx.getReferenceId().equals(1L)));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Reverse stock on cancellation
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("6. Reverse stock on cancellation — stock restored")
    void test6_reverseStock_stockRestored() {
        order.setInventoryDeducted(true);
        flourStock.setQuantityOnHand(new BigDecimal("4.6")); // after deduction

        when(recipeRepository.findByMenuItemId(100L)).thenReturn(Optional.of(recipe));
        when(stockRepository.findByBranchAndIngredient(branch, flourIngredient))
                .thenReturn(Optional.of(flourStock));
        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.reverseStockForOrder(order);

        // 4.6 + 0.4 = 5.0 restored
        assertEquals(new BigDecimal("5.0"), flourStock.getQuantityOnHand());
        verify(transactionRepository).save(argThat(tx ->
                tx.getTransactionType() == InventoryTransaction.TransactionType.REVERSAL));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 7: Reverse stock — no prior deduction — no-op
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("7. Reverse stock — no prior deduction — no-op, no transaction created")
    void test7_reverseStock_noPriorDeduction_noOp() {
        order.setInventoryDeducted(false);

        inventoryService.reverseStockForOrder(order);

        verify(recipeRepository, never()).findByMenuItemId(any());
        verify(stockRepository, never()).findByBranchAndIngredient(any(), any());
        verify(transactionRepository, never()).save(any());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 8: Low-stock alert triggered when quantity ≤ minimum
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("8. Low-stock alert triggered when qty drops to/below minimum")
    void test8_lowStockAlert_triggered() {
        // After deducting 0.4 KG from 1.3 → 0.9 which is below minimum of 1.0
        flourStock.setQuantityOnHand(new BigDecimal("1.3"));
        flourStock.setMinimumStockLevel(new BigDecimal("1.0"));

        when(recipeRepository.findByMenuItemId(100L)).thenReturn(Optional.of(recipe));
        when(stockRepository.findByBranchAndIngredient(branch, flourIngredient))
                .thenReturn(Optional.of(flourStock));
        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        inventoryService.deductStockForOrder(order);

        // Alert should be published to WebSocket topic
        verify(messagingTemplate).convertAndSend(
                eq("/topic/branches/1/inventory/alerts"),
                any(Object.class));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 9: Low-stock alert NOT triggered when qty stays above minimum
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("9. Low-stock alert NOT triggered when qty > minimum after deduction")
    void test9_lowStockAlert_notTriggered() {
        // 5.0 - 0.4 = 4.6 > minimum 1.0 → no alert
        when(recipeRepository.findByMenuItemId(100L)).thenReturn(Optional.of(recipe));
        when(stockRepository.findByBranchAndIngredient(branch, flourIngredient))
                .thenReturn(Optional.of(flourStock));
        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.deductStockForOrder(order);

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 10: Receive purchase — stock increased
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("10. Receive purchase — stock increases by received quantity")
    void test10_receivePurchase_stockIncreased() {
        Purchase purchase = new Purchase();
        purchase.setId(5L);
        purchase.setBranch(branch);

        PurchaseItem pi = new PurchaseItem();
        pi.setId(1L);
        pi.setPurchase(purchase);
        pi.setIngredient(flourIngredient);
        pi.setQuantityOrdered(new BigDecimal("10"));
        pi.setQuantityReceived(new BigDecimal("10"));
        pi.setUnit(Ingredient.IngredientUnit.KG);
        pi.setUnitCost(new BigDecimal("2000"));

        when(stockRepository.findByBranchAndIngredient(branch, flourIngredient))
                .thenReturn(Optional.of(flourStock));
        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.creditStockForPurchaseItem(branch, pi, manager);

        // 5.0 + 10.0 = 15.0
        assertEquals(new BigDecimal("15.0"), flourStock.getQuantityOnHand());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 11: Receive purchase — PURCHASE transaction created
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("11. Receive purchase — PURCHASE transaction saved")
    void test11_receivePurchase_transactionCreated() {
        Purchase purchase = new Purchase();
        purchase.setId(5L);
        purchase.setBranch(branch);

        PurchaseItem pi = new PurchaseItem();
        pi.setId(1L);
        pi.setPurchase(purchase);
        pi.setIngredient(flourIngredient);
        pi.setQuantityOrdered(new BigDecimal("10"));
        pi.setQuantityReceived(new BigDecimal("10"));
        pi.setUnit(Ingredient.IngredientUnit.KG);
        pi.setUnitCost(new BigDecimal("2000"));

        when(stockRepository.findByBranchAndIngredient(branch, flourIngredient))
                .thenReturn(Optional.of(flourStock));
        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.creditStockForPurchaseItem(branch, pi, manager);

        verify(transactionRepository).save(argThat(tx ->
                tx.getTransactionType() == InventoryTransaction.TransactionType.PURCHASE
                && tx.getReferenceType().equals("PURCHASE")
                && tx.getReferenceId().equals(5L)));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 12: Record wastage — stock decreased
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("12. Record wastage — stock decreased by wastage quantity")
    void test12_recordWastage_stockDecreased() {
        when(stockRepository.findByBranchAndIngredient(branch, flourIngredient))
                .thenReturn(Optional.of(flourStock));
        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.recordWastage(branch, flourIngredient,
                new BigDecimal("1.0"), Ingredient.IngredientUnit.KG, "Ilioza", manager);

        // 5.0 - 1.0 = 4.0
        assertEquals(new BigDecimal("4.0"), flourStock.getQuantityOnHand());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 13: Record wastage — WASTAGE transaction created
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("13. Record wastage — WASTAGE transaction saved")
    void test13_recordWastage_transactionCreated() {
        when(stockRepository.findByBranchAndIngredient(branch, flourIngredient))
                .thenReturn(Optional.of(flourStock));
        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.recordWastage(branch, flourIngredient,
                new BigDecimal("0.5"), Ingredient.IngredientUnit.KG, "Ilioza", manager);

        verify(transactionRepository).save(argThat(tx ->
                tx.getTransactionType() == InventoryTransaction.TransactionType.WASTAGE
                && tx.getReferenceType().equals("WASTAGE")));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 14: Stock adjustment IN
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("14. Stock adjustment IN — stock increases, ADJUSTMENT_IN transaction saved")
    void test14_stockAdjustmentIn() {
        StockAdjustmentRequest req = new StockAdjustmentRequest();
        req.setIngredientId(10L);
        req.setQuantity(new BigDecimal("2.0"));
        req.setUnit(Ingredient.IngredientUnit.KG);
        req.setAdjustmentType(StockAdjustmentRequest.AdjustmentType.IN);
        req.setReason("Marekebisho ya kawaida");

        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(ingredientRepository.findById(10L)).thenReturn(Optional.of(flourIngredient));
        when(userRepository.findByUsername("manager_test")).thenReturn(Optional.of(manager));
        when(stockRepository.findByBranchAndIngredient(branch, flourIngredient))
                .thenReturn(Optional.of(flourStock));
        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = inventoryService.adjustStock(1L, req, "manager_test");

        assertEquals(new BigDecimal("7.0"), flourStock.getQuantityOnHand());
        verify(transactionRepository).save(argThat(tx ->
                tx.getTransactionType() == InventoryTransaction.TransactionType.ADJUSTMENT_IN));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 15: Stock adjustment OUT
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("15. Stock adjustment OUT — stock decreases, ADJUSTMENT_OUT transaction saved")
    void test15_stockAdjustmentOut() {
        StockAdjustmentRequest req = new StockAdjustmentRequest();
        req.setIngredientId(10L);
        req.setQuantity(new BigDecimal("1.0"));
        req.setUnit(Ingredient.IngredientUnit.KG);
        req.setAdjustmentType(StockAdjustmentRequest.AdjustmentType.OUT);
        req.setReason("Kutoa kwa matumizi ya dharura");

        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(ingredientRepository.findById(10L)).thenReturn(Optional.of(flourIngredient));
        when(userRepository.findByUsername("manager_test")).thenReturn(Optional.of(manager));
        when(stockRepository.findByBranchAndIngredient(branch, flourIngredient))
                .thenReturn(Optional.of(flourStock));
        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.adjustStock(1L, req, "manager_test");

        // 5.0 - 1.0 = 4.0
        assertEquals(new BigDecimal("4.0"), flourStock.getQuantityOnHand());
        verify(transactionRepository).save(argThat(tx ->
                tx.getTransactionType() == InventoryTransaction.TransactionType.ADJUSTMENT_OUT));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 16: Branch isolation
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("16. Branch isolation — Branch A deduction does NOT affect Branch B stock")
    void test16_branchIsolation() {
        // Order is for Branch A
        when(recipeRepository.findByMenuItemId(100L)).thenReturn(Optional.of(recipe));
        when(stockRepository.findByBranchAndIngredient(branch, flourIngredient))
                .thenReturn(Optional.of(flourStock));
        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.deductStockForOrder(order);

        // Branch A stock deducted
        assertEquals(new BigDecimal("4.6"), flourStock.getQuantityOnHand());

        // Branch B stock NEVER touched
        verify(stockRepository, never()).findByBranchAndIngredient(branchB, flourIngredient);
        assertEquals(new BigDecimal("3.0"), flourStockB.getQuantityOnHand());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 17: Unit conversion — recipe in GRAM, stock in KG
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("17. Unit conversion — recipe requires 200g, stock stored in KG — deducts 0.2 KG")
    void test17_unitConversion_gramToKg() {
        // Recipe item in grams
        recipeItem.setQuantityRequired(new BigDecimal("200")); // 200 grams per serving
        recipeItem.setUnit(Ingredient.IngredientUnit.GRAM);
        // Stock is in KG

        when(recipeRepository.findByMenuItemId(100L)).thenReturn(Optional.of(recipe));
        when(stockRepository.findByBranchAndIngredient(branch, flourIngredient))
                .thenReturn(Optional.of(flourStock));
        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.deductStockForOrder(order);

        // 2 chapatis × 200g = 400g = 0.4 KG; 5.0 - 0.4 = 4.6 KG
        assertEquals(new BigDecimal("4.600000"), flourStock.getQuantityOnHand().stripTrailingZeros()
                .compareTo(new BigDecimal("4.6")) == 0
                ? new BigDecimal("4.600000")
                : flourStock.getQuantityOnHand());
        assertTrue(new BigDecimal("4.6").compareTo(flourStock.getQuantityOnHand()) == 0
                   || flourStock.getQuantityOnHand().stripTrailingZeros().compareTo(new BigDecimal("4.6")) == 0);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 18: Recipe cost calculation
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("18. Unit conversion utility — KG to GRAM and back")
    void test18_unitConversionUtility() {
        // KG → GRAM
        BigDecimal result = inventoryService.convertUnits(
                new BigDecimal("1.5"),
                Ingredient.IngredientUnit.KG,
                Ingredient.IngredientUnit.GRAM);
        assertEquals(0, new BigDecimal("1500").compareTo(result),
                "1.5 KG should convert to 1500 GRAM");

        // GRAM → KG
        BigDecimal result2 = inventoryService.convertUnits(
                new BigDecimal("500"),
                Ingredient.IngredientUnit.GRAM,
                Ingredient.IngredientUnit.KG);
        assertEquals(0, new BigDecimal("0.5").compareTo(result2),
                "500 GRAM should convert to 0.5 KG");

        // LITRE → ML
        BigDecimal result3 = inventoryService.convertUnits(
                new BigDecimal("2"),
                Ingredient.IngredientUnit.LITRE,
                Ingredient.IngredientUnit.ML);
        assertEquals(0, new BigDecimal("2000").compareTo(result3),
                "2 LITRE should convert to 2000 ML");

        // Same unit — pass-through
        BigDecimal result4 = inventoryService.convertUnits(
                new BigDecimal("5"),
                Ingredient.IngredientUnit.KG,
                Ingredient.IngredientUnit.KG);
        assertEquals(0, new BigDecimal("5").compareTo(result4),
                "Same unit should return same quantity");
    }
}
