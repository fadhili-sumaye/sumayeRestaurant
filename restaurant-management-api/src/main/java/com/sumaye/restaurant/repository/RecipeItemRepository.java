package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Recipe;
import com.sumaye.restaurant.model.RecipeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeItemRepository extends JpaRepository<RecipeItem, Long> {
    List<RecipeItem> findByRecipe(Recipe recipe);
    void deleteByRecipe(Recipe recipe);
}
