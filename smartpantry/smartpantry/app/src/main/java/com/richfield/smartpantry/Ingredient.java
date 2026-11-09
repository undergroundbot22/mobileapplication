package com.richfield.smartpantry;

@SuppressWarnings("unused")
public class Ingredient {
    private String name;
    private double quantity;
    private String unit;

    public Ingredient() {
    }

    public Ingredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public Ingredient(String name, double quantity) {
        this(name, quantity, "");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
