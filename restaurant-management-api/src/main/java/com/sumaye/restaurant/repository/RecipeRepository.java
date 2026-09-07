package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.MenuItem;
import com.sumaye.restaurant.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    Optional<Recipe> findByMenuItem(MenuItem menuItem);
    Optional<Recipe> findByMenuItemId(Long menuItemId);
}
