package com.restaurant.service;

import com.restaurant.config.DBConnection;
import com.restaurant.entity.CategoryEnum;
import com.restaurant.entity.Dish;
import com.restaurant.entity.DishTypeEnum;
import com.restaurant.entity.Ingredient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private final DBConnection dbConnection = new DBConnection();
    
    // a) Dish findDishById(Integer id)
    public Dish findDishById(int id) {
        try {
            Connection con = dbConnection.getConnection();
            PreparedStatement dishStmt = con.prepareStatement("SELECT * FROM \"Dish\" WHERE id = ?");
            dishStmt.setInt(1, id);
            ResultSet dishRs = dishStmt.executeQuery();
            
            Dish dish = null;
            if (dishRs.next()) {
                dish = new Dish(dishRs.getInt("id"), dishRs.getString("name"), 
                               DishTypeEnum.valueOf(dishRs.getString("dish_type")));
                
                PreparedStatement ingStmt = con.prepareStatement("SELECT * FROM \"Ingredient\" WHERE id_dish = ?");
                ingStmt.setInt(1, id);
                ResultSet ingRs = ingStmt.executeQuery();
                
                while (ingRs.next()) {
                    dish.getIngredient().add(new Ingredient(
                        ingRs.getInt("id"), ingRs.getString("name"),
                        ingRs.getDouble("price"), CategoryEnum.valueOf(ingRs.getString("category"))
                    ));
                }
                ingRs.close();
                ingStmt.close();
            }
            
            dishRs.close();
            dishStmt.close();
            dbConnection.closeConnection(con);
            return dish;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    // b) List<Ingredient> findIngredients(int page, int size)
    public List<Ingredient> findIngredients(int page, int size) {
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            Connection con = dbConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM \"Ingredient\" LIMIT ? OFFSET ?");
            stmt.setInt(1, size);
            stmt.setInt(2, (page - 1) * size);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ingredients.add(new Ingredient(rs.getInt("id"), rs.getString("name"),
                    rs.getDouble("price"), CategoryEnum.valueOf(rs.getString("category"))));
            }
            
            rs.close();
            stmt.close();
            dbConnection.closeConnection(con);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ingredients;
    }
    
    // c) List<Ingredient> createIngredients(List<Ingredient> newIngredients)
    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) throws SQLException {
        Connection con = null;
        try {
            List<String> existingNames = getIngredientsName();
            for (Ingredient ing : newIngredients) {
                if (existingNames.contains(ing.getName())) {
                    throw new RuntimeException("L'ingrédient existe déjà");
                }
            }
            
            con = dbConnection.getConnection();
            con.setAutoCommit(false);
            
            PreparedStatement stmt = con.prepareStatement(
                "INSERT INTO \"Ingredient\"(name, price, category, id_dish) VALUES (?, ?, ?::ingredient_category_enum, ?)");
            
            for (Ingredient ing : newIngredients) {
                stmt.setString(1, ing.getName());
                stmt.setDouble(2, ing.getPrice());
                stmt.setString(3, ing.getCategory().toString());
                stmt.setObject(4, ing.getDish() != null ? ing.getDish().getId() : null);
                stmt.executeUpdate();
            }
            
            con.commit();
            stmt.close();
            con.setAutoCommit(true);
            dbConnection.closeConnection(con);
            return newIngredients;
        } catch (Exception e) {
            if (con != null) con.rollback();
            throw new RuntimeException(e);
        }
    }
    
    private List<String> getIngredientsName() {
        List<String> names = new ArrayList<>();
        try {
            Connection con = dbConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT name FROM \"Ingredient\"");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) names.add(rs.getString("name"));
            rs.close();
            stmt.close();
            dbConnection.closeConnection(con);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return names;
    }
    
    // d) Dish saveDish(Dish dishToSave)
    public Dish saveDish(Dish dishToSave) throws SQLException {
        Connection con = null;
        try {
            con = dbConnection.getConnection();
            con.setAutoCommit(false);
            
            PreparedStatement checkStmt = con.prepareStatement("SELECT id FROM \"Dish\" WHERE id = ?");
            checkStmt.setInt(1, dishToSave.getId());
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                PreparedStatement insertStmt = con.prepareStatement(
                    "INSERT INTO \"Dish\" (name, dish_type) VALUES (?, ?::dish_type_enum)");
                insertStmt.setString(1, dishToSave.getName());
                insertStmt.setString(2, dishToSave.getDishType().toString());
                insertStmt.executeUpdate();
                insertStmt.close();
            } else {
                PreparedStatement updateStmt = con.prepareStatement(
                    "UPDATE \"Dish\" SET name = ?, dish_type = ?::dish_type_enum WHERE id = ?");
                updateStmt.setString(1, dishToSave.getName());
                updateStmt.setString(2, dishToSave.getDishType().toString());
                updateStmt.setInt(3, dishToSave.getId());
                updateStmt.executeUpdate();
                updateStmt.close();
                
                PreparedStatement deleteStmt = con.prepareStatement("DELETE FROM \"Ingredient\" WHERE id_dish = ?");
                deleteStmt.setInt(1, dishToSave.getId());
                deleteStmt.executeUpdate();
                deleteStmt.close();
            }
            
            PreparedStatement ingStmt = con.prepareStatement(
                "INSERT INTO \"Ingredient\"(name, price, category, id_dish) VALUES (?, ?, ?::ingredient_category_enum, ?)");
            for (Ingredient ing : dishToSave.getIngredient()) {
                ingStmt.setString(1, ing.getName());
                ingStmt.setDouble(2, ing.getPrice());
                ingStmt.setString(3, ing.getCategory().toString());
                ingStmt.setInt(4, dishToSave.getId());
                ingStmt.executeUpdate();
            }
            
            con.commit();
            rs.close();
            checkStmt.close();
            ingStmt.close();
            con.setAutoCommit(true);
            dbConnection.closeConnection(con);
            return dishToSave;
        } catch (Exception e) {
            if (con != null) con.rollback();
            throw new RuntimeException(e);
        }
    }
    
    // e) List<Dish> findDishesByIngredientName(String ingredientName)
    public List<Dish> findDishesByIngredientName(String ingredientName) {
        List<Dish> dishes = new ArrayList<>();
        try {
            Connection con = dbConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(
                "SELECT DISTINCT d.* FROM \"Dish\" d JOIN \"Ingredient\" i ON d.id = i.id_dish WHERE i.name = ?");
            stmt.setString(1, ingredientName);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Dish dish = new Dish(rs.getInt("id"), rs.getString("name"),
                    DishTypeEnum.valueOf(rs.getString("dish_type")));
                dish.setIngredient(getIngredientsOfDish(dish.getId(), con));
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
    
    private List<Ingredient> getIngredientsOfDish(int dishId, Connection con) {
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM \"Ingredient\" WHERE id_dish = ?");
            stmt.setInt(1, dishId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ingredients.add(new Ingredient(rs.getInt("id"), rs.getString("name"),
                    rs.getDouble("price"), CategoryEnum.valueOf(rs.getString("category"))));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ingredients;
    }
    
    // f) List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category, String dishName, int page, int size)
    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category, String dishName, int page, int size) {
        List<Ingredient> ingredients = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT i.* FROM \"Ingredient\" i JOIN \"Dish\" d ON i.id_dish = d.id WHERE 1=1");
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
                ingredients.add(new Ingredient(rs.getInt("id"), rs.getString("name"),
                    rs.getDouble("price"), CategoryEnum.valueOf(rs.getString("category"))));
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