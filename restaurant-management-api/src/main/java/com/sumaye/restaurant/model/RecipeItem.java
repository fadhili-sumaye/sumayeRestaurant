package com.sumaye.restaurant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "recipe_items")
@Data
@NoArgsConstructor
public class RecipeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    @JsonIgnore
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    /**
     * How much of the ingredient is needed per ONE serving of this menu item.
     */
    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal quantityRequired;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Ingredient.IngredientUnit unit;
}
