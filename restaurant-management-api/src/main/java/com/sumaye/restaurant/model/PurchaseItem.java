package com.sumaye.restaurant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_items")
@Data
@NoArgsConstructor
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    @JsonIgnore
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal quantityOrdered;

    @Column(precision = 14, scale = 4)
    private BigDecimal quantityReceived = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Ingredient.IngredientUnit unit;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;
}
