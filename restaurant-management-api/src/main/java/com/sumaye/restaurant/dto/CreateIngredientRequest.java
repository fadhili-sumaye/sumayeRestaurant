package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Ingredient;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class CreateIngredientRequest {

    @NotBlank(message = "Jina la kiungo linahitajika")
    private String name;

    private String description;

    private String kiswahiliName;

    private String category;

    @NotNull(message = "Kitengo cha kupima kinahitajika")
    private Ingredient.IngredientUnit defaultUnit;
}
