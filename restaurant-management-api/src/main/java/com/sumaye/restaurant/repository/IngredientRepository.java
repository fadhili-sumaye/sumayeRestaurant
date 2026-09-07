package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    List<Ingredient> findByActiveTrue();
    List<Ingredient> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    Optional<Ingredient> findByNameIgnoreCase(String name);
}
