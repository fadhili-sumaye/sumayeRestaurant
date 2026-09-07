package com.sumaye.restaurant.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_stock",
        uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id", "ingredient_id"}))
@Data
@NoArgsConstructor
public class InventoryStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Prevent concurrent stock updates from silently overwriting one another. */
    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal quantityOnHand = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Ingredient.IngredientUnit unit = Ingredient.IngredientUnit.KG;

    /**
     * When quantityOnHand <= minimumStockLevel a low-stock alert is triggered.
     */
    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal minimumStockLevel = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    @Column(precision = 14, scale = 4)
    private BigDecimal maximumStockLevel;

    @Column(precision = 10, scale = 2)
    private BigDecimal costPerUnit = BigDecimal.ZERO;

    @Column
    private LocalDateTime lastRestockedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;
}
