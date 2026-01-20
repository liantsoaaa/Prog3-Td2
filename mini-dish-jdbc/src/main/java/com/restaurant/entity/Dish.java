package com.restaurant.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Dish {
    private int id;
    private String name;
    private DishTypeEnum dishType;
    private Double sellingPrice;
    private List<DishIngredient> dishIngredients = new ArrayList<>();

    public Dish(int id, String name, DishTypeEnum dishType, List<DishIngredient> dishIngredients) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.dishIngredients = dishIngredients;
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

    public List<DishIngredient> getDishIngredients() {
        return dishIngredients;
    }

    public Double getSellingPrice() {
        return sellingPrice;
    }

    public Double getDishCost() {
        Double totalCost = 0.0;
        for (DishIngredient di : dishIngredients) {
            if (di.getIngredient() != null && di.getQuantityRequired() != null) {
                totalCost += di.getIngredient().getPrice() * di.getQuantityRequired();
            }
        }
        return totalCost;
    }


    public Double getGrossMargin() {
        if (sellingPrice == null) {
            throw new RuntimeException("Le prix de vente est null");
        }
        return sellingPrice - getDishCost();
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

    public void setDishIngredients(List<DishIngredient> dishIngredients) {
        this.dishIngredients = dishIngredients;
    }

    public void setSellingPrice(Double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return id == dish.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "\n  Id=" + id +
                "\n  Name='" + name + '\'' +
                "\n  DishType=" + dishType +
                "\n  SellingPrice=" + sellingPrice + " Ar" +
                "\n  DishCost=" + getDishCost() + " Ar" +
                (sellingPrice != null ? "\n  GrossMargin=" + getGrossMargin() + " Ar" : "") +
                "\n  Ingredients=" + dishIngredients.size() +
                (!dishIngredients.isEmpty() ? "\n  " + dishIngredients : "") +
                "\n}";
    }


}