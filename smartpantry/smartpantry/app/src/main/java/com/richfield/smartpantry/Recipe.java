package com.richfield.smartpantry;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Recipe {
    private int id;
    private String name;
    private List<Ingredient> requiredIngredients;
    private String steps;

    public Recipe() {
        this.requiredIngredients = new ArrayList<>();
    }

    public Recipe(int id, String name, List<Ingredient> requiredIngredients, String steps) {
        this.id = id;
        this.name = name;
        if (requiredIngredients != null) {
            this.requiredIngredients = new ArrayList<>(requiredIngredients);
        } else {
            this.requiredIngredients = new ArrayList<>();
        }
        this.steps = steps;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Ingredient> getRequiredIngredients() {
        return requiredIngredients;
    }

    public void setRequiredIngredients(List<Ingredient> requiredIngredients) {
        this.requiredIngredients = requiredIngredients;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }
}
