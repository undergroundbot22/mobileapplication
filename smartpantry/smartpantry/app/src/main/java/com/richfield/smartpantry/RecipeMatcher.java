package com.richfield.smartpantry;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "ReassignedVariable", "IfCanBeSwitch"})
public class RecipeMatcher {

    public static List<Recipe> getStrictMatches(List<Recipe> allRecipes, List<PantryItem> pantryItems) {
        List<Recipe> matchedRecipes = new ArrayList<>();

        if (allRecipes == null || pantryItems == null) {
            return matchedRecipes;
        }

        for (Recipe recipe : allRecipes) {
            if (hasAllIngredients(recipe, pantryItems)) {
                matchedRecipes.add(recipe);
            }
        }
        return matchedRecipes;
    }

    public static List<Recipe> getStringMatches(List<Recipe> allRecipes, List<PantryItem> pantryItems) {
        return getStrictMatches(allRecipes, pantryItems);
    }

    public static List<Recipe> getAlmostThereMatches(List<Recipe> allRecipes, List<PantryItem> pantryItems) {
        List<Recipe> almostRecipes = new ArrayList<>();

        if (allRecipes == null || pantryItems == null) {
            return almostRecipes;
        }

        for (Recipe recipe : allRecipes) {
            if (countMissingIngredients(recipe, pantryItems) == 1) {
                almostRecipes.add(recipe);
            }
        }
        return almostRecipes;
    }

    public static Ingredient getSingleMissingIngredient(Recipe recipe, List<PantryItem> pantryItems) {
        if (recipe == null || recipe.getRequiredIngredients() == null || pantryItems == null) {
            return null;
        }

        for (Ingredient requiredIng : recipe.getRequiredIngredients()) {
            boolean found = false;
            for (PantryItem pantryItem : pantryItems) {
                if (normalizeName(pantryItem.getName()).equalsIgnoreCase(normalizeName(requiredIng.getName()))
                        && pantryItem.getQuantity() >= requiredIng.getQuantity()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return requiredIng;
            }
        }
        return null;
    }

    private static boolean hasAllIngredients(Recipe recipe, List<PantryItem> pantryItems) {
        List<Ingredient> ingredients = recipe.getRequiredIngredients();
        if (ingredients == null) {
            return false;
        }

        for (Ingredient requiredIng : ingredients) {
            boolean hasIngredientInQuantity = false;

            for (PantryItem pantryItem : pantryItems) {
                String normPantry = normalizeName(pantryItem.getName());
                String normRequired = normalizeName(requiredIng.getName());

                if (normPantry.equalsIgnoreCase(normRequired)
                        && pantryItem.getQuantity() >= requiredIng.getQuantity()) {
                    hasIngredientInQuantity = true;
                    break;
                }
            }

            if (!hasIngredientInQuantity) {
                return false;
            }
        }
        return true;
    }

    private static int countMissingIngredients(Recipe recipe, List<PantryItem> pantryItems) {
        List<Ingredient> ingredients = recipe.getRequiredIngredients();
        if (ingredients == null) {
            return -1;
        }

        int missingCount = 0;
        for (Ingredient requiredIng : ingredients) {
            boolean found = false;

            for (PantryItem pantryItem : pantryItems) {
                if (normalizeName(pantryItem.getName()).equalsIgnoreCase(normalizeName(requiredIng.getName()))
                        && pantryItem.getQuantity() >= requiredIng.getQuantity()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missingCount++;
            }
        }
        return missingCount;
    }

    public static String normalizeName(String name) {
        if (name == null) return "";
        String normalized = name.trim().toLowerCase();
        if (normalized.endsWith("es")) {
            return normalized.substring(0, normalized.length() - 2);
        } else if (normalized.endsWith("s")) {
            return normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
