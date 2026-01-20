package com.restaurant.entity;

import java.util.Objects;

public class DishIngredient {
    private int id;
    private Dish dish;
    private Ingredient ingredient;
    private Double quantityRequired;
    private String unit;

    public DishIngredient() {
    }

    public DishIngredient(int id, Dish dish, Ingredient ingredient, Double quantityRequired, String unit) {
        this.id = id;
        this.dish = dish;
        this.ingredient = ingredient;
        this.quantityRequired = quantityRequired;
        this.unit = unit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Double getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(Double quantityRequired) {
        this.quantityRequired = quantityRequired;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DishIngredient that = (DishIngredient) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "\n    DishIngredient{" +
                "ingredient=" + (ingredient != null ? ingredient.getName() : "null") +
                ", quantity=" + quantityRequired +
                ", unit='" + unit + '\'' +
                '}';
    }
}