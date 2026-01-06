package com.restaurant;

import com.restaurant.service.DataRetriever;
import com.restaurant.entity.Dish;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        DataRetriever retriever = new DataRetriever();
        
        // Test recuperer un plat
        Dish dish = retriever.findDishById(1);
        
        if (dish != null) {
            System.out.println("test reussi!");
            System.out.println("Plat trouvé : " + dish.getName());
            System.out.println("Nombre d'ingrédients : " + dish.getIngredient().size());
        } else {
            System.out.println("Aucun plat trouvé");
        }
    }
}
