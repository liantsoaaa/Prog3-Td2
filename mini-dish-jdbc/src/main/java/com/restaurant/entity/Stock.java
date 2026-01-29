package com.restaurant.entity;

import java.time.Instant;

public class Stock {
    private Ingredient ingredient;
    private Double quantity;
    private String unit;
    private Instant calculatedAt;

    public Stock() {
    }

    public Stock(Ingredient ingredient, Double quantity, String unit, Instant calculatedAt) {
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
        this.calculatedAt = calculatedAt;
    }

    // Getters et Setters
    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Instant calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}