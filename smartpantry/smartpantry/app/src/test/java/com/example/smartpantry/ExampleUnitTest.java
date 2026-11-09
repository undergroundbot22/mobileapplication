package com.example.smartpantry;

import com.richfield.smartpantry.Ingredient;
import com.richfield.smartpantry.PantryItem;
import com.richfield.smartpantry.Recipe;
import com.richfield.smartpantry.RecipeMatcher;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ExampleUnitTest {

    @Test
    public void testStrictRecipeMatch() {
        Recipe pasta = new Recipe(1, "Pasta", Arrays.asList(
                new Ingredient("Pasta", 200, "g"),
                new Ingredient("Tomato", 2, "pcs")
        ), "Boil and mix.");

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("pasta", 300, "g", "2026-12-01"),
                new PantryItem("tomatoes", 3, "pcs", "2026-12-01")
        );

        List<Recipe> strictMatches = RecipeMatcher.getStrictMatches(Arrays.asList(pasta), pantry);
        assertEquals(1, strictMatches.size());
        assertEquals("Pasta", strictMatches.get(0).getName());
    }

    @Test
    public void testAlmostThereRecipeMatch() {
        Recipe pasta = new Recipe(1, "Pasta", Arrays.asList(
                new Ingredient("Pasta", 200, "g"),
                new Ingredient("Tomato", 2, "pcs"),
                new Ingredient("Garlic", 1, "cloves")
        ), "Boil and mix.");

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("pasta", 200, "g", "2026-12-01"),
                new PantryItem("tomato", 2, "pcs", "2026-12-01")
        );

        List<Recipe> strict = RecipeMatcher.getStrictMatches(Arrays.asList(pasta), pantry);
        assertEquals(0, strict.size());

        List<Recipe> almost = RecipeMatcher.getAlmostThereMatches(Arrays.asList(pasta), pantry);
        assertEquals(1, almost.size());

        Ingredient missing = RecipeMatcher.getSingleMissingIngredient(pasta, pantry);
        assertNotNull(missing);
        assertEquals("Garlic", missing.getName());
    }

    @Test
    public void testNormalization() {
        assertEquals("tomato", RecipeMatcher.normalizeName("Tomatoes"));
        assertEquals("egg", RecipeMatcher.normalizeName("eggs"));
        assertEquals("milk", RecipeMatcher.normalizeName(" Milk "));
    }
}
