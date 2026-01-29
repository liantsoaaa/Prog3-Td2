package com.restaurant.entity;

import java.time.Instant;
import java.util.List;

public class Order {
    private Integer id;
    private String reference;
    private Instant creationDatetime;
    private List<DishOrder> dishOrders;

    public Order() {
    }

    public Order(Integer id, String reference, Instant creationDatetime, List<DishOrder> dishOrders) {
        this.id = id;
        this.reference = reference;
        this.creationDatetime = creationDatetime;
        this.dishOrders = dishOrders;
    }

    // montant total HT
    public Double getTotalAmountWithoutVAT() {
        if (dishOrders == null || dishOrders.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (DishOrder dishOrder : dishOrders) {
            if (dishOrder.getDish() != null && dishOrder.getDish().getPrice() != null) {
                total += dishOrder.getDish().getPrice() * dishOrder.getQuantity();
            }
        }
        return total;
    }

    // montant total TTC
    public Double getTotalAmountWithVAT() {
        double htAmount = getTotalAmountWithoutVAT();
        return htAmount * 1.20;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }

    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }
}