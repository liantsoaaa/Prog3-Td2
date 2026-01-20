package com.restaurant.service;

import com.restaurant.config.DBConnection;
import com.restaurant.entity.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private final DBConnection dbConnection = new DBConnection();

    public Dish findDishById(int id) {
        try {
            Connection con = dbConnection.getConnection();
            PreparedStatement dishStmt = con.prepareStatement("SELECT * FROM dish WHERE id = ?");
            dishStmt.setInt(1, id);
            ResultSet dishRs = dishStmt.executeQuery();

            Dish dish = null;
            if (dishRs.next()) {
                dish = new Dish(
                        dishRs.getInt("id"),
                        dishRs.getString("name"),
                        DishTypeEnum.valueOf(dishRs.getString("dish_type"))
                );

                Double sellingPrice = dishRs.getObject("selling_price") != null ?
                        dishRs.getDouble("selling_price") : null;
                dish.setSellingPrice(sellingPrice);

                dish.setDishIngredients(findDishIngredientsByDishId(id, con));
            }

            dishRs.close();
            dishStmt.close();
            dbConnection.closeConnection(con);
            return dish;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<DishIngredient> findDishIngredientsByDishId(Integer dishId) {
        try {
            Connection con = dbConnection.getConnection();
            List<DishIngredient> result = findDishIngredientsByDishId(dishId, con);
            dbConnection.closeConnection(con);
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<DishIngredient> findDishIngredientsByDishId(int dishId, Connection con) {
        List<DishIngredient> dishIngredients = new ArrayList<>();
        try {
            String sql = """
                SELECT di.id_dish, di.id_ingredient, di.quantity_required, di.unit, 
                       i.id as ing_id, i.name, i.price, i.category 
                FROM dishingredient di 
                LEFT JOIN ingredient i ON di.id_ingredient = i.id 
                WHERE di.id_dish = ?
            """;

            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, dishId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Ingredient ingredient = new Ingredient(
                        rs.getInt("ing_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category"))
                );

                DishIngredient di = new DishIngredient(
                        rs.getInt("id_dish"),
                        null,
                        ingredient,
                        rs.getDouble("quantity_required"),
                        rs.getString("unit")
                );
                dishIngredients.add(di);
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dishIngredients;
    }

    public List<Ingredient> findIngredients(int page, int size) {
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            Connection con = dbConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM ingredient LIMIT ? OFFSET ?");
            stmt.setInt(1, size);
            stmt.setInt(2, (page - 1) * size);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ingredients.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category"))
                ));
            }

            rs.close();
            stmt.close();
            dbConnection.closeConnection(con);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ingredients;
    }

    public List<Dish> findDishesByIngredientName(String ingredientName) {
        List<Dish> dishes = new ArrayList<>();
        try {
            Connection con = dbConnection.getConnection();
            String sql = """
                SELECT DISTINCT d.* FROM dish d 
                JOIN dishingredient di ON d.id = di.id_dish 
                JOIN ingredient i ON di.id_ingredient = i.id 
                WHERE i.name = ?
            """;

            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, ingredientName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Dish dish = new Dish(
                        rs.getInt("id"),
                        rs.getString("name"),
                        DishTypeEnum.valueOf(rs.getString("dish_type"))
                );
                Double sellingPrice = rs.getObject("selling_price") != null ?
                        rs.getDouble("selling_price") : null;
                dish.setSellingPrice(sellingPrice);
                dish.setDishIngredients(findDishIngredientsByDishId(dish.getId(), con));
                dishes.add(dish);
            }

            rs.close();
            stmt.close();
            dbConnection.closeConnection(con);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dishes;
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category, String dishName, int page, int size) {
        List<Ingredient> ingredients = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT i.* FROM ingredient i 
            JOIN dishingredient di ON i.id = di.id_ingredient 
            JOIN dish d ON di.id_dish = d.id WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();

        if (ingredientName != null && !ingredientName.trim().isEmpty()) {
            sql.append(" AND i.name ILIKE ?");
            params.add("%" + ingredientName + "%");
        }
        if (category != null) {
            sql.append(" AND i.category = ?::ingredient_category_enum");
            params.add(category.toString());
        }
        if (dishName != null && !dishName.trim().isEmpty()) {
            sql.append(" AND d.name ILIKE ?");
            params.add("%" + dishName + "%");
        }
        sql.append(" LIMIT ? OFFSET ?");

        try {
            Connection con = dbConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql.toString());

            int index = 1;
            for (Object param : params) stmt.setObject(index++, param);
            stmt.setInt(index++, size);
            stmt.setInt(index, (page - 1) * size);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ingredients.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category"))
                ));
            }

            rs.close();
            stmt.close();
            dbConnection.closeConnection(con);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ingredients;
    }
}
