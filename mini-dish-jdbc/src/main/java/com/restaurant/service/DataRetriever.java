package com.restaurant.service;

import com.restaurant.config.DBConnection;
import com.restaurant.entity.*;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private final DBConnection dbConnection = new DBConnection();

    //TD2 + TD3 -------------------------------------------------------------------------------------------------------

    /**
     * a) Récupère un plat par son ID avec tous ses ingrédients
     */
    public Dish findDishById(Integer id) {
        try {
            Connection con = dbConnection.getConnection();
            Dish dish = findDishById(id, con);
            dbConnection.closeConnection(con);
            return dish;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Version de findDishById qui utilise une connexion existante
     */
    private Dish findDishById(Integer id, Connection con) throws SQLException {
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

            // Récupérer le price si disponible
            if (dishRs.getObject("price") != null) {
                dish.setPrice(dishRs.getDouble("price"));
            }

            dish.setDishIngredients(findDishIngredientsByDishId(id, con));
        } else {
            throw new RuntimeException("Plat avec ID " + id + " introuvable");
        }

        dishRs.close();
        dishStmt.close();
        return dish;
    }

    /**
     * b) Liste les ingrédients avec pagination
     */
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

    /**
     * c) Crée de nouveaux ingrédients (atomique : tout ou rien)
     */
    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) throws SQLException {
        Connection con = null;
        try {
            List<String> existingNames = getIngredientsName();

            for (Ingredient ing : newIngredients) {
                if (existingNames.contains(ing.getName())) {
                    throw new RuntimeException("L'ingrédient existe déjà: " + ing.getName());
                }
            }

            con = dbConnection.getConnection();
            con.setAutoCommit(false);

            PreparedStatement stmt = con.prepareStatement(
                    "INSERT INTO ingredient(name, price, category) VALUES (?, ?, ?::ingredient_category_enum)",
                    Statement.RETURN_GENERATED_KEYS);

            for (Ingredient ing : newIngredients) {
                stmt.setString(1, ing.getName());
                stmt.setDouble(2, ing.getPrice());
                stmt.setString(3, ing.getCategory().toString());
                stmt.executeUpdate();

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    ing.setId(generatedKeys.getInt(1));
                }
                generatedKeys.close();
            }

            con.commit();
            stmt.close();
            con.setAutoCommit(true);
            dbConnection.closeConnection(con);
            return newIngredients;
        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * d) Sauvegarde un plat (insert ou update) avec ses ingrédients
     */
    public Dish saveDish(Dish dishToSave) throws SQLException {
        Connection con = null;
        try {
            con = dbConnection.getConnection();
            con.setAutoCommit(false);

            boolean isUpdate = false;

            if (dishToSave.getId() > 0) {
                PreparedStatement checkStmt = con.prepareStatement("SELECT id FROM dish WHERE id = ?");
                checkStmt.setInt(1, dishToSave.getId());
                ResultSet rs = checkStmt.executeQuery();
                isUpdate = rs.next();
                rs.close();
                checkStmt.close();
            }

            if (!isUpdate) {
                PreparedStatement insertStmt = con.prepareStatement(
                        "INSERT INTO dish (name, dish_type, selling_price) VALUES (?, ?::dish_type_enum, ?) RETURNING id");
                insertStmt.setString(1, dishToSave.getName());
                insertStmt.setString(2, dishToSave.getDishType().toString());
                if (dishToSave.getSellingPrice() != null) {
                    insertStmt.setDouble(3, dishToSave.getSellingPrice());
                } else {
                    insertStmt.setNull(3, Types.DOUBLE);
                }

                ResultSet generatedKeys = insertStmt.executeQuery();
                if (generatedKeys.next()) {
                    dishToSave.setId(generatedKeys.getInt(1));
                }
                generatedKeys.close();
                insertStmt.close();

            } else {
                PreparedStatement updateStmt = con.prepareStatement(
                        "UPDATE dish SET name = ?, dish_type = ?::dish_type_enum, selling_price = ? WHERE id = ?");
                updateStmt.setString(1, dishToSave.getName());
                updateStmt.setString(2, dishToSave.getDishType().toString());
                if (dishToSave.getSellingPrice() != null) {
                    updateStmt.setDouble(3, dishToSave.getSellingPrice());
                } else {
                    updateStmt.setNull(3, Types.DOUBLE);
                }
                updateStmt.setInt(4, dishToSave.getId());
                updateStmt.executeUpdate();
                updateStmt.close();

                PreparedStatement deleteStmt = con.prepareStatement("DELETE FROM dishingredient WHERE id_dish = ?");
                deleteStmt.setInt(1, dishToSave.getId());
                deleteStmt.executeUpdate();
                deleteStmt.close();
            }

            if (dishToSave.getDishIngredients() != null && !dishToSave.getDishIngredients().isEmpty()) {
                PreparedStatement diStmt = con.prepareStatement(
                        "INSERT INTO dishingredient(id_dish, id_ingredient, quantity_required, unit) VALUES (?, ?, ?, ?)");

                for (DishIngredient di : dishToSave.getDishIngredients()) {
                    diStmt.setInt(1, dishToSave.getId());
                    diStmt.setInt(2, di.getIngredient().getId());
                    diStmt.setDouble(3, di.getQuantityRequired());
                    diStmt.setString(4, di.getUnit());
                    diStmt.executeUpdate();
                }
                diStmt.close();
            }

            con.commit();
            con.setAutoCommit(true);
            dbConnection.closeConnection(con);

            return findDishById(dishToSave.getId());
        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * e) Recherche des plats par nom d'ingrédient (contient)
     */
    public List<Dish> findDishesByIngredientName(String ingredientName) {
        List<Dish> dishes = new ArrayList<>();
        try {
            Connection con = dbConnection.getConnection();
            String sql = "SELECT DISTINCT d.* FROM dish d " +
                    "JOIN dishingredient di ON d.id = di.id_dish " +
                    "JOIN ingredient i ON di.id_ingredient = i.id " +
                    "WHERE i.name ILIKE ?";

            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, "%" + ingredientName + "%");
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

    /**
     * f) Recherche des ingrédients par critères avec pagination
     */
    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category, String dishName, int page, int size) {
        List<Ingredient> ingredients = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT i.* FROM ingredient i " +
                        "JOIN dishingredient di ON i.id = di.id_ingredient " +
                        "JOIN dish d ON di.id_dish = d.id WHERE 1=1"
        );
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

    //TD4 -------------------------------------------------------------------------------------------------------------

    /**
     * TD4 - a) Sauvegarde un ingrédient avec ses mouvements de stock
     */
    public Ingredient saveIngredient(Ingredient toSave) throws SQLException {
        Connection con = null;
        try {
            con = dbConnection.getConnection();
            con.setAutoCommit(false);

            boolean isUpdate = false;

            if (toSave.getId() > 0) {
                PreparedStatement checkStmt = con.prepareStatement("SELECT id FROM ingredient WHERE id = ?");
                checkStmt.setInt(1, toSave.getId());
                ResultSet rs = checkStmt.executeQuery();
                isUpdate = rs.next();
                rs.close();
                checkStmt.close();
            }

            if (!isUpdate) {
                PreparedStatement insertStmt = con.prepareStatement(
                        "INSERT INTO ingredient (name, price, category) VALUES (?, ?, ?::ingredient_category_enum) RETURNING id");
                insertStmt.setString(1, toSave.getName());
                insertStmt.setDouble(2, toSave.getPrice());
                insertStmt.setString(3, toSave.getCategory().toString());

                ResultSet generatedKeys = insertStmt.executeQuery();
                if (generatedKeys.next()) {
                    toSave.setId(generatedKeys.getInt(1));
                }
                generatedKeys.close();
                insertStmt.close();
            } else {
                PreparedStatement updateStmt = con.prepareStatement(
                        "UPDATE ingredient SET name = ?, price = ?, category = ?::ingredient_category_enum WHERE id = ?");
                updateStmt.setString(1, toSave.getName());
                updateStmt.setDouble(2, toSave.getPrice());
                updateStmt.setString(3, toSave.getCategory().toString());
                updateStmt.setInt(4, toSave.getId());
                updateStmt.executeUpdate();
                updateStmt.close();
            }

            if (toSave.getStockMovementList() != null && !toSave.getStockMovementList().isEmpty()) {
                for (StockMovement sm : toSave.getStockMovementList()) {
                    if (sm.getId() != null && sm.getId() > 0) {
                        PreparedStatement smStmt = con.prepareStatement(
                                "INSERT INTO stock_movement (id, id_ingredient, quantity, unit, movement_date) " +
                                        "VALUES (?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING");
                        smStmt.setInt(1, sm.getId());
                        smStmt.setInt(2, toSave.getId());
                        smStmt.setDouble(3, sm.getQuantity());
                        smStmt.setString(4, sm.getUnit());
                        smStmt.setTimestamp(5, Timestamp.from(sm.getMovementDate()));
                        smStmt.executeUpdate();
                        smStmt.close();
                    } else {
                        PreparedStatement insertSm = con.prepareStatement(
                                "INSERT INTO stock_movement (id_ingredient, quantity, unit, movement_date) " +
                                        "VALUES (?, ?, ?, ?) RETURNING id");
                        insertSm.setInt(1, toSave.getId());
                        insertSm.setDouble(2, sm.getQuantity());
                        insertSm.setString(3, sm.getUnit());
                        insertSm.setTimestamp(4, Timestamp.from(sm.getMovementDate()));

                        ResultSet rs = insertSm.executeQuery();
                        if (rs.next()) {
                            sm.setId(rs.getInt(1));
                        }
                        rs.close();
                        insertSm.close();
                    }
                }
            }

            con.commit();
            con.setAutoCommit(true);
            dbConnection.closeConnection(con);

            return toSave;
        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * TD4 - b) Calcule le stock d'un ingrédient à une date donnée (normalisé en KG)
     */
    public Stock getStockValueAt(Integer ingredientId, Instant t) {
        try {
            Connection con = dbConnection.getConnection();
            Ingredient ingredient = findIngredientById(ingredientId, con);

            // Récupérer TOUS les mouvements jusqu'à la date t
            String sql = "SELECT quantity, unit " +
                    "FROM stock_movement " +
                    "WHERE id_ingredient = ? AND movement_date <= ?";

            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, ingredientId);
            stmt.setTimestamp(2, Timestamp.from(t));
            ResultSet rs = stmt.executeQuery();

            // Calculer le total en convertissant tout en KG
            double totalInKg = 0.0;

            while (rs.next()) {
                double quantity = rs.getDouble("quantity");
                String unit = rs.getString("unit");

                // Convertir en KG
                double quantityInKg = UnitConversion.convertToKg(
                        ingredient.getName(),
                        quantity,
                        unit
                );

                totalInKg += quantityInKg;
            }

            rs.close();
            stmt.close();
            dbConnection.closeConnection(con);

            // Créer le stock avec le résultat en KG
            Stock stock = new Stock();
            stock.setIngredient(ingredient);
            stock.setCalculatedAt(t);
            stock.setUnit("KG");
            stock.setQuantity(totalInKg);

            return stock;
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
            String sql = "SELECT di.id_dish, di.id_ingredient, di.quantity_required, di.unit, " +
                    "i.id as ing_id, i.name, i.price, i.category " +
                    "FROM dishingredient di " +
                    "LEFT JOIN ingredient i ON di.id_ingredient = i.id " +
                    "WHERE di.id_dish = ?";

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

                DishIngredient di = new DishIngredient();
                di.setIngredient(ingredient);
                di.setQuantityRequired(rs.getDouble("quantity_required"));
                di.setUnit(rs.getString("unit"));

                dishIngredients.add(di);
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dishIngredients;
    }

    private Ingredient findIngredientById(Integer id, Connection con) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("SELECT * FROM ingredient WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        Ingredient ingredient = null;
        if (rs.next()) {
            ingredient = new Ingredient(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    CategoryEnum.valueOf(rs.getString("category"))
            );
        }

        rs.close();
        stmt.close();
        return ingredient;
    }

    private List<String> getIngredientsName() {
        List<String> names = new ArrayList<>();
        try {
            Connection con = dbConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT name FROM ingredient");
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

    // TD5 ------------------------------------------------------------------------------------------------------------

    /**
     * TD5 - i) Sauvegarde une commande avec vérification des stocks
     */
    public Order saveOrder(Order orderToSave) throws SQLException {
        Connection con = null;
        try {
            con = dbConnection.getConnection();
            con.setAutoCommit(false);

            // 1. Vérifier la disponibilité des stocks pour tous les plats
            Instant now = Instant.now();
            for (DishOrder dishOrder : orderToSave.getDishOrders()) {
                Dish dish = findDishById(dishOrder.getDish().getId(), con);

                // Vérifier chaque ingrédient du plat
                for (DishIngredient di : dish.getDishIngredients()) {
                    Stock stock = getStockValueAt(di.getIngredient().getId(), now);
                    double requiredQuantity = di.getQuantityRequired() * dishOrder.getQuantity();

                    if (stock.getQuantity() < requiredQuantity) {
                        con.rollback();
                        dbConnection.closeConnection(con);
                        throw new RuntimeException(
                                "Stock insuffisant pour l'ingrédient: " + di.getIngredient().getName() +
                                        " (Disponible: " + stock.getQuantity() + " " + stock.getUnit() +
                                        ", Requis: " + requiredQuantity + " " + di.getUnit() + ")"
                        );
                    }
                }
            }

            boolean isUpdate = false;

            // 2. Vérifier si c'est une mise à jour
            if (orderToSave.getId() != null && orderToSave.getId() > 0) {
                PreparedStatement checkStmt = con.prepareStatement("SELECT id FROM \"order\" WHERE id = ?");
                checkStmt.setInt(1, orderToSave.getId());
                ResultSet rs = checkStmt.executeQuery();
                isUpdate = rs.next();
                rs.close();
                checkStmt.close();
            }

            if (!isUpdate) {
                // 3. Générer la référence si elle n'existe pas (EN JAVA)
                if (orderToSave.getReference() == null || orderToSave.getReference().isEmpty()) {
                    PreparedStatement countStmt = con.prepareStatement("SELECT COUNT(*) FROM \"order\"");
                    ResultSet countRs = countStmt.executeQuery();
                    int nextNumber = 1;
                    if (countRs.next()) {
                        nextNumber = countRs.getInt(1) + 1;
                    }
                    countRs.close();
                    countStmt.close();

                    orderToSave.setReference(String.format("ORD%05d", nextNumber));
                }

                // 4. Insérer la commande
                PreparedStatement insertOrder = con.prepareStatement(
                        "INSERT INTO \"order\" (reference, creation_datetime) VALUES (?, ?) RETURNING id"
                );
                insertOrder.setString(1, orderToSave.getReference());
                insertOrder.setTimestamp(2, Timestamp.from(orderToSave.getCreationDatetime() != null ?
                        orderToSave.getCreationDatetime() : Instant.now()));

                ResultSet rs = insertOrder.executeQuery();
                if (rs.next()) {
                    orderToSave.setId(rs.getInt(1));
                }
                rs.close();
                insertOrder.close();

            } else {
                // Mise à jour de la commande
                PreparedStatement updateOrder = con.prepareStatement(
                        "UPDATE \"order\" SET reference = ?, creation_datetime = ? WHERE id = ?"
                );
                updateOrder.setString(1, orderToSave.getReference());
                updateOrder.setTimestamp(2, Timestamp.from(orderToSave.getCreationDatetime()));
                updateOrder.setInt(3, orderToSave.getId());
                updateOrder.executeUpdate();
                updateOrder.close();

                // Supprimer les anciens dishorders
                PreparedStatement deleteStmt = con.prepareStatement("DELETE FROM dishorder WHERE id_order = ?");
                deleteStmt.setInt(1, orderToSave.getId());
                deleteStmt.executeUpdate();
                deleteStmt.close();
            }

            // 5. Insérer les dishorders
            if (orderToSave.getDishOrders() != null && !orderToSave.getDishOrders().isEmpty()) {
                PreparedStatement insertDishOrder = con.prepareStatement(
                        "INSERT INTO dishorder (id_order, id_dish, quantity) VALUES (?, ?, ?) RETURNING id"
                );

                for (DishOrder dishOrder : orderToSave.getDishOrders()) {
                    insertDishOrder.setInt(1, orderToSave.getId());
                    insertDishOrder.setInt(2, dishOrder.getDish().getId());
                    insertDishOrder.setInt(3, dishOrder.getQuantity());

                    ResultSet rs = insertDishOrder.executeQuery();
                    if (rs.next()) {
                        dishOrder.setId(rs.getInt(1));
                    }
                    rs.close();
                }
                insertDishOrder.close();
            }

            // 6. Déduire les stocks (créer des mouvements négatifs)
            PreparedStatement insertMovement = con.prepareStatement(
                    "INSERT INTO stock_movement (id_ingredient, quantity, unit, movement_date) VALUES (?, ?, ?, ?)"
            );

            for (DishOrder dishOrder : orderToSave.getDishOrders()) {
                Dish dish = findDishById(dishOrder.getDish().getId(), con);

                for (DishIngredient di : dish.getDishIngredients()) {
                    double quantityToDeduct = di.getQuantityRequired() * dishOrder.getQuantity();

                    insertMovement.setInt(1, di.getIngredient().getId());
                    insertMovement.setDouble(2, -quantityToDeduct);
                    insertMovement.setString(3, di.getUnit());
                    insertMovement.setTimestamp(4, Timestamp.from(Instant.now()));
                    insertMovement.addBatch();
                }
            }
            insertMovement.executeBatch();
            insertMovement.close();

            con.commit();
            con.setAutoCommit(true);

            String reference = orderToSave.getReference();
            dbConnection.closeConnection(con);

            return findOrderByReference(reference);

        } catch (Exception e) {
            if (con != null) {
                try {
                    if (!con.isClosed()) {
                        con.rollback();
                        dbConnection.closeConnection(con);
                    }
                } catch (SQLException ex) {
                    // Ignorer
                }
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * TD5 - ii) Récupère une commande par sa référence
     */
    public Order findOrderByReference(String reference) {
        try {
            Connection con = dbConnection.getConnection();

            // Récupérer la commande
            PreparedStatement orderStmt = con.prepareStatement(
                    "SELECT * FROM \"order\" WHERE reference = ?"
            );
            orderStmt.setString(1, reference);
            ResultSet orderRs = orderStmt.executeQuery();

            Order order = null;
            if (orderRs.next()) {
                order = new Order();
                order.setId(orderRs.getInt("id"));
                order.setReference(orderRs.getString("reference"));
                order.setCreationDatetime(orderRs.getTimestamp("creation_datetime").toInstant());

                // Récupérer les dishorders
                order.setDishOrders(findDishOrdersByOrderId(order.getId(), con));
            } else {
                dbConnection.closeConnection(con);
                throw new RuntimeException("Commande avec référence " + reference + " introuvable");
            }

            orderRs.close();
            orderStmt.close();
            dbConnection.closeConnection(con);

            return order;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Méthode auxiliaire pour récupérer les dishorders d'une commande
     */
    private List<DishOrder> findDishOrdersByOrderId(int orderId, Connection con) {
        List<DishOrder> dishOrders = new ArrayList<>();
        try {
            String sql = "SELECT id, id_dish, quantity " +
                    "FROM dishorder " +
                    "WHERE id_order = ?";

            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                DishOrder dishOrder = new DishOrder();
                dishOrder.setId(rs.getInt("id"));
                dishOrder.setQuantity(rs.getInt("quantity"));

                // Récupérer le plat complet
                Dish dish = findDishById(rs.getInt("id_dish"), con);
                dishOrder.setDish(dish);

                dishOrders.add(dishOrder);
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dishOrders;
    }

    /**
     * TD6 - Réinitialise les stocks pour les tests
     */
    public void resetStocksForTesting() throws SQLException {
        Connection con = null;
        try {
            con = dbConnection.getConnection();
            con.setAutoCommit(false);

            // Supprimer tous les mouvements
            PreparedStatement deleteStmt = con.prepareStatement("DELETE FROM stock_movement");
            deleteStmt.executeUpdate();
            deleteStmt.close();

            // Réinsérer le stock initial
            PreparedStatement insertStmt = con.prepareStatement(
                    "INSERT INTO stock_movement (id_ingredient, quantity, unit, movement_date) " +
                            "SELECT id, ?, 'KG', ? FROM ingredient WHERE name = ?"
            );

            Object[][] stocksInitiaux = {
                    {"Tomate", 4.0},
                    {"Laitue", 5.0},
                    {"Poulet", 10.0},
                    {"Chocolat", 3.0},
                    {"Beurre", 2.5},
                    {"Fromage", 35.0}
            };

            Instant dateInitiale = Instant.parse("2024-01-01T00:00:00Z");

            for (Object[] stock : stocksInitiaux) {
                insertStmt.setDouble(1, (Double) stock[1]);
                insertStmt.setTimestamp(2, Timestamp.from(dateInitiale));
                insertStmt.setString(3, (String) stock[0]);
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();
            insertStmt.close();

            con.commit();
            con.setAutoCommit(true);
            dbConnection.closeConnection(con);

            System.out.println("Stocks réinitialisés avec succès");

        } catch (Exception e) {
            if (con != null) {
                try {
                    if (!con.isClosed()) {
                        con.rollback();
                        dbConnection.closeConnection(con);
                    }
                } catch (SQLException ex) {

                }
            }
            throw new RuntimeException(e);
        }
    }
}