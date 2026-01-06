package com.restaurant.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dish {
    private int id;
    private String name;
    private DishTypeEnum dishType;
    private List<Ingredient> ingredient = new ArrayList<>();

    public Dish(int id, String name, DishTypeEnum dishType, List<Ingredient> ingredient) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.ingredient = ingredient;
    }
    public Dish(int id, String name, DishTypeEnum dishType) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
    }
    public Dish() {
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public List<Ingredient> getIngredient() {
        return ingredient;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public void setIngredient(List<Ingredient> ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return id == dish.id && Objects.equals(name, dish.name) && dishType == dish.dishType && Objects.equals(ingredient, dish.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dishType, ingredient);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "\nId=" + id +
                "\nName='" + name + '\'' +
                "\nDishType=" + dishType +
                (!ingredient.isEmpty() ? "\n\t" + ingredient.toString() : "\n") +
                '}';
    }

}
