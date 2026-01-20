package com.restaurant;

import com.restaurant.entity.*;
import com.restaurant.service.DataRetriever;

public class Main {
    public static void main(String[] args) {
        DataRetriever dr = new DataRetriever();

        System.out.println("=== TESTS TD3 ===\n");

        // Test 1: Coût des plats
        System.out.println("COÛTS:");
        testPlat(dr, 1, "Salade fraîche", 250);
        testPlat(dr, 2, "Poulet grillé", 4500);
        testPlat(dr, 4, "Gâteau au chocolat", 1400);
        System.out.println();

        // Test 2: Marge brute
        System.out.println("MARGES:");
        testMarge(dr, 1, "Salade fraîche", 3250);
        testMarge(dr, 2, "Poulet grillé", 7500);
        testMarge(dr, 4, "Gâteau au chocolat", 6600);
        System.out.println();

        // Test 3: Ingrédients
        System.out.println("INGRÉDIENTS:");
        showIngredients(dr, 1);
        showIngredients(dr, 4);
    }

    static void testPlat(DataRetriever dr, int id, String nom, double attendu) {
        try {
            double got = dr.findDishById(id).getDishCost();
            System.out.println(nom + ": " + got + " Ar " + (Math.abs(got-attendu)<1 ? "✓" : "✗"));
        } catch (Exception e) {
            System.out.println(nom + ": ERREUR");
        }
    }

    static void testMarge(DataRetriever dr, int id, String nom, double attendu) {
        try {
            double got = dr.findDishById(id).getGrossMargin();
            System.out.println(nom + ": " + got + " Ar " + (Math.abs(got-attendu)<1 ? "✓" : "✗"));
        } catch (Exception e) {
            System.out.println(nom + ": ERREUR");
        }
    }

    static void showIngredients(DataRetriever dr, int id) {
        try {
            var ings = dr.findDishIngredientsByDishId(id);
            for (var ing : ings) {
                System.out.println("  " + ing.getIngredient().getName() +
                        ": " + ing.getQuantityRequired() + ing.getUnit());
            }
        } catch (Exception e) {
            System.out.println("ERREUR ingrédients");
        }
        System.out.println();
    }
}
