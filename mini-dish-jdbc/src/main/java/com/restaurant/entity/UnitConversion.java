package com.restaurant.entity;

import java.util.HashMap;
import java.util.Map;

public class UnitConversion {
    private String ingredientName;
    private Double kgToPcs;
    private Double kgToL;

    private static final Map<String, UnitConversion> CONVERSIONS = new HashMap<>();

    static {
        CONVERSIONS.put("Tomate", new UnitConversion("Tomate", 10.0, null));
        CONVERSIONS.put("Laitue", new UnitConversion("Laitue", 2.0, null));
        CONVERSIONS.put("Chocolat", new UnitConversion("Chocolat", 10.0, 2.5));
        CONVERSIONS.put("Poulet", new UnitConversion("Poulet", 8.0, null));
        CONVERSIONS.put("Beurre", new UnitConversion("Beurre", 4.0, 5.0));
    }

    public UnitConversion(String ingredientName, Double kgToPcs, Double kgToL) {
        this.ingredientName = ingredientName;
        this.kgToPcs = kgToPcs;
        this.kgToL = kgToL;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public Double getKgToPcs() {
        return kgToPcs;
    }

    public Double getKgToL() {
        return kgToL;
    }

    public static UnitConversion getConversion(String ingredientName) {
        return CONVERSIONS.get(ingredientName);
    }

    public static double convert(String ingredientName, double quantity, String unitSource, String unitTarget) {
        if (unitSource.equalsIgnoreCase(unitTarget)) {
            return quantity;
        }

        UnitConversion conversion = CONVERSIONS.get(ingredientName);
        if (conversion == null) {
            throw new RuntimeException("FAIL: " + ingredientName);
        }

        double quantityInKg;

        switch (unitSource.toUpperCase()) {
            case "KG":
                quantityInKg = quantity;
                break;
            case "PCS":
                if (conversion.getKgToPcs() == null) {
                    throw new RuntimeException("Conversion PCS impossible pour: " + ingredientName);
                }
                quantityInKg = quantity / conversion.getKgToPcs();
                break;
            case "L":
                if (conversion.getKgToL() == null) {
                    throw new RuntimeException("Conversion L impossible pour: " + ingredientName);
                }
                quantityInKg = quantity / conversion.getKgToL();
                break;
            default:
                throw new RuntimeException("Unité inconnue: " + unitSource);
        }

        switch (unitTarget.toUpperCase()) {
            case "KG":
                return quantityInKg;
            case "PCS":
                if (conversion.getKgToPcs() == null) {
                    throw new RuntimeException("Conversion PCS impossible pour: " + ingredientName);
                }
                return quantityInKg * conversion.getKgToPcs();
            case "L":
                if (conversion.getKgToL() == null) {
                    throw new RuntimeException("Conversion L impossible pour: " + ingredientName);
                }
                return quantityInKg * conversion.getKgToL();
            default:
                throw new RuntimeException("Unité inconnue: " + unitTarget);
        }
    }

    public static double convertToKg(String ingredientName, double quantity, String unit) {
        return convert(ingredientName, quantity, unit, "KG");
    }
}