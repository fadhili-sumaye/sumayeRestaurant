package com.sumaye.restaurant.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Data
@NoArgsConstructor
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType transactionType;

    /**
     * Positive = stock IN, Negative = stock OUT
     */
    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal quantityChange;

    /**
     * Stock level AFTER this transaction was applied.
     */
    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal quantityAfter;

    /** Stock level immediately before this audited movement. */
    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal quantityBefore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Ingredient.IngredientUnit unit;

    /**
     * e.g. "ORDER", "PURCHASE", "WASTAGE", "ADJUSTMENT"
     */
    @Column(length = 30)
    private String referenceType;

    /**
     * ID of the related Order / Purchase / WastageRecord etc.
     */
    @Column
    private Long referenceId;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TransactionType {
        PURCHASE,
        SALE_CONSUMPTION,
        WASTAGE,
        ADJUSTMENT_IN,
        ADJUSTMENT_OUT,
        RETURN,
        REVERSAL,
        TRANSFER_IN,
        TRANSFER_OUT
    }
}
