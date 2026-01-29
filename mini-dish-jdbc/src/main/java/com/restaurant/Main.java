package com.restaurant;

import com.restaurant.entity.*;
import com.restaurant.service.DataRetriever;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataRetriever dr = new DataRetriever();

        System.out.println("=== TESTS TD2 + TD3 ===\n");

        testA(dr);
        testB(dr);
        testC(dr);
        testD(dr);
        testE(dr);
        testF(dr);
        testG(dr);
        testH(dr);
        testI(dr);
        testJ(dr);
        testK(dr);
        testL(dr);
        testM(dr);

        System.out.println("\n=== TESTS TD4 - GESTION DE STOCKS ===\n");
        testTD4(dr);

        System.out.println("\n=== TESTS TD5 - GESTION DES COMMANDES ===\n");
        testTD5_CreateOrder(dr);
        testTD5_FindOrder(dr);
        testTD5_InsufficientStock(dr);

        System.out.println("\n=== TESTS TD6 - CONVERSIONS D'UNITÉS ===\n");
        testTD6_UnitConversion(dr);

        System.out.println("\n=== TESTS ÉVALUATION - GESTION DES TABLES ===\n");
        testEval_CreateOrderWithTable(dr);
        testEval_TableNotAvailable(dr);
        testEval_NoTableAvailable(dr);


        System.out.println("\n=== FIN DES TESTS ===");
    }

    private static void testA(DataRetriever dr) {
        System.out.println("Test a) findDishById(1)");
        try {
            Dish dish = dr.findDishById(1);
            System.out.println(" Plat: " + dish.getName());
            System.out.println("  Ingrédients: " + dish.getDishIngredients().size());
            for (DishIngredient di : dish.getDishIngredients()) {
                System.out.println("  - " + di.getIngredient().getName());
            }
        } catch (Exception e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testB(DataRetriever dr) {
        System.out.println("Test b) findDishById(999)");
        try {
            dr.findDishById(999);
            System.out.println("FAIL");
        } catch (RuntimeException e) {
            System.out.println("Exception levée: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testC(DataRetriever dr) {
        System.out.println("Test c) findIngredients(page=2, size=2)");
        try {
            List<Ingredient> ingredients = dr.findIngredients(2, 2);
            System.out.println("Nombre: " + ingredients.size());
            for (Ingredient ing : ingredients) {
                System.out.println("  - " + ing.getName());
            }
        } catch (Exception e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testD(DataRetriever dr) {
        System.out.println("Test d) findIngredients(page=3, size=5)");
        try {
            List<Ingredient> ingredients = dr.findIngredients(3, 5);
            System.out.println("Nombre: " + ingredients.size());
        } catch (Exception e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testE(DataRetriever dr) {
        System.out.println("Test e) findDishesByIngredientName('eur')");
        try {
            List<Dish> dishes = dr.findDishesByIngredientName("eur");
            System.out.println("Nombre: " + dishes.size());
            for (Dish dish : dishes) {
                System.out.println("  - " + dish.getName());
            }
        } catch (Exception e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testF(DataRetriever dr) {
        System.out.println("Test f) findIngredientsByCriteria(VEGETABLE)");
        try {
            List<Ingredient> ingredients = dr.findIngredientsByCriteria(
                    null, CategoryEnum.VEGETABLE, null, 1, 10);
            System.out.println("Nombre: " + ingredients.size());
            for (Ingredient ing : ingredients) {
                System.out.println("  - " + ing.getName());
            }
        } catch (Exception e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testG(DataRetriever dr) {
        System.out.println("Test g) findIngredientsByCriteria('cho', 'Sal')");
        try {
            List<Ingredient> ingredients = dr.findIngredientsByCriteria(
                    "cho", null, "Sal", 1, 10);
            System.out.println("Nombre: " + ingredients.size());
        } catch (Exception e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testH(DataRetriever dr) {
        System.out.println("Test h) findIngredientsByCriteria('cho', 'gâteau')");
        try {
            List<Ingredient> ingredients = dr.findIngredientsByCriteria(
                    "cho", null, "gâteau", 1, 10);
            System.out.println("Nombre: " + ingredients.size());
            for (Ingredient ing : ingredients) {
                System.out.println("  - " + ing.getName());
            }
        } catch (Exception e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testI(DataRetriever dr) {
        System.out.println("Test i) createIngredients(Fromage, Oignon)");
        try {
            List<Ingredient> existing = dr.findIngredients(1, 100);
            boolean fromageExists = existing.stream().anyMatch(i -> i.getName().equals("Fromage"));
            boolean oignonExists = existing.stream().anyMatch(i -> i.getName().equals("Oignon"));

            if (fromageExists && oignonExists) {
                System.out.println("ℹ Déjà créés");
            } else {
                List<Ingredient> newIngredients = new ArrayList<>();
                if (!fromageExists) newIngredients.add(new Ingredient("Fromage", 1200.0, CategoryEnum.DAIRY));
                if (!oignonExists) newIngredients.add(new Ingredient("Oignon", 500.0, CategoryEnum.VEGETABLE));

                List<Ingredient> created = dr.createIngredients(newIngredients);
                System.out.println("PASS " + created.size() + " créé(s)");
            }
        } catch (Exception e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testJ(DataRetriever dr) {
        System.out.println("Test j) createIngredients(Carotte, Laitue)");
        try {
            List<Ingredient> newIngredients = new ArrayList<>();
            newIngredients.add(new Ingredient("Carotte", 2000.0, CategoryEnum.VEGETABLE));
            newIngredients.add(new Ingredient("Laitue", 2000.0, CategoryEnum.VEGETABLE));

            dr.createIngredients(newIngredients);
            System.out.println("FAIL");
        } catch (RuntimeException e) {
            System.out.println("PASS: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testK(DataRetriever dr) {
        System.out.println("Test k) saveDish(Soupe de légumes)");
        try {
            List<Ingredient> allIngredients = dr.findIngredients(1, 100);
            Ingredient oignon = allIngredients.stream()
                    .filter(i -> i.getName().equals("Oignon"))
                    .findFirst().orElse(null);

            if (oignon != null) {
                Dish newDish = new Dish();
                newDish.setName("Soupe de légumes");
                newDish.setDishType(DishTypeEnum.START);

                List<DishIngredient> dishIngredients = new ArrayList<>();
                DishIngredient di = new DishIngredient();
                di.setIngredient(oignon);
                di.setQuantityRequired(0.1);
                di.setUnit("KG");
                dishIngredients.add(di);
                newDish.setDishIngredients(dishIngredients);

                Dish saved = dr.saveDish(newDish);
                System.out.println("Plat créé: " + saved.getName() + " (ID: " + saved.getId() + ")");
            }
        } catch (Exception e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testL(DataRetriever dr) {
        System.out.println("Test l) saveDish - Créer puis Update");
        try {
            List<Ingredient> allIngredients = dr.findIngredients(1, 100);
            Ingredient laitue = allIngredients.stream().filter(i -> i.getName().equals("Laitue")).findFirst().orElse(null);
            Ingredient tomate = allIngredients.stream().filter(i -> i.getName().equals("Tomate")).findFirst().orElse(null);

            Dish newDish = new Dish();
            newDish.setName("Test Salade");
            newDish.setDishType(DishTypeEnum.START);

            List<DishIngredient> dishIngredients = new ArrayList<>();
            if (laitue != null) {
                DishIngredient di = new DishIngredient();
                di.setIngredient(laitue);
                di.setQuantityRequired(0.2);
                di.setUnit("KG");
                dishIngredients.add(di);
            }
            if (tomate != null) {
                DishIngredient di = new DishIngredient();
                di.setIngredient(tomate);
                di.setQuantityRequired(0.15);
                di.setUnit("KG");
                dishIngredients.add(di);
            }
            newDish.setDishIngredients(dishIngredients);

            Dish saved = dr.saveDish(newDish);
            System.out.println("Plat créé: " + saved.getName() + " (ID: " + saved.getId() + ")");
            System.out.println("  Ingrédients: " + saved.getDishIngredients().size());

            Ingredient oignon = allIngredients.stream().filter(i -> i.getName().equals("Oignon")).findFirst().orElse(null);
            if (oignon != null) {
                DishIngredient di = new DishIngredient();
                di.setIngredient(oignon);
                di.setQuantityRequired(0.05);
                di.setUnit("KG");
                saved.getDishIngredients().add(di);
            }

            Dish updated = dr.saveDish(saved);
            System.out.println("Plat mis à jour");
            System.out.println("  Ingrédients: " + updated.getDishIngredients().size());

        } catch (Exception e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testM(DataRetriever dr) {
        System.out.println("Test m) saveDish - Modifier ingrédients");
        try {
            List<Ingredient> allIngredients = dr.findIngredients(1, 100);
            Ingredient fromage = allIngredients.stream().filter(i -> i.getName().equals("Fromage")).findFirst().orElse(null);

            Dish newDish = new Dish();
            newDish.setName("Test Fromage");
            newDish.setDishType(DishTypeEnum.START);

            List<DishIngredient> dishIngredients = new ArrayList<>();
            if (fromage != null) {
                DishIngredient di = new DishIngredient();
                di.setIngredient(fromage);
                di.setQuantityRequired(0.2);
                di.setUnit("KG");
                dishIngredients.add(di);
            }
            newDish.setDishIngredients(dishIngredients);

            Dish saved = dr.saveDish(newDish);
            System.out.println("Plat créé avec " + saved.getDishIngredients().size() + " ingrédient(s)");

        } catch (Exception e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testTD4(DataRetriever dr) {
        System.out.println("--- Test TD4: Stock au 2024-01-06 12:00 ---");
        try {
            Instant testDate = Instant.parse("2024-01-06T12:00:00Z");

            List<Ingredient> allIngredients = dr.findIngredients(1, 100);
            String[] ingredientsToTest = {"Tomate", "Laitue", "Poulet", "Chocolat", "Beurre", "Fromage"};

            for (String ingredientName : ingredientsToTest) {
                Ingredient ing = allIngredients.stream()
                        .filter(i -> i.getName().equals(ingredientName))
                        .findFirst()
                        .orElse(null);

                if (ing != null) {
                    Stock stock = dr.getStockValueAt(ing.getId(), testDate);
                    System.out.printf("  %s: %.2f %s%n",
                            ingredientName,
                            stock.getQuantity(),
                            stock.getUnit());
                }
            }

            System.out.println("✓ Calcul des stocks terminé");

        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testTD5_CreateOrder(DataRetriever dr) {
        System.out.println("--- Test TD5.1: Créer une commande ---");
        try {
            Order newOrder = new Order();
            newOrder.setCreationDatetime(Instant.now());

            List<DishOrder> dishOrders = new ArrayList<>();

            Dish dish1 = dr.findDishById(1);
            DishOrder do1 = new DishOrder();
            do1.setDish(dish1);
            do1.setQuantity(2);
            dishOrders.add(do1);

            Dish dish2 = dr.findDishById(2);
            DishOrder do2 = new DishOrder();
            do2.setDish(dish2);
            do2.setQuantity(1);
            dishOrders.add(do2);

            newOrder.setDishOrders(dishOrders);

            Order saved = dr.saveOrder(newOrder);
            System.out.println("✓ Commande créée: " + saved.getReference());
            System.out.println("  Montant HT: " + saved.getTotalAmountWithoutVAT() + " Ar");
            System.out.println("  Montant TTC: " + saved.getTotalAmountWithVAT() + " Ar");
            System.out.println("  Plats commandés: " + saved.getDishOrders().size());

        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void testTD5_FindOrder(DataRetriever dr) {
        System.out.println("--- Test TD5.2: Rechercher une commande ---");
        try {
            Order order = dr.findOrderByReference("ORD00001");
            System.out.println("✓ Commande trouvée: " + order.getReference());
            System.out.println("  Date: " + order.getCreationDatetime());
            System.out.println("  Montant TTC: " + order.getTotalAmountWithVAT() + " Ar");

        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testTD5_InsufficientStock(DataRetriever dr) {
        System.out.println("--- Test TD5.3: Stock insuffisant ---");
        try {
            Order newOrder = new Order();
            newOrder.setCreationDatetime(Instant.now());

            List<DishOrder> dishOrders = new ArrayList<>();

            Dish dish = dr.findDishById(1);
            DishOrder do1 = new DishOrder();
            do1.setDish(dish);
            do1.setQuantity(1000);
            dishOrders.add(do1);

            newOrder.setDishOrders(dishOrders);

            dr.saveOrder(newOrder);
            System.out.println("✗ FAIL: Devrait lever une exception");

        } catch (RuntimeException e) {
            System.out.println("✓ Exception levée (attendu): " + e.getMessage());
        } catch (Exception e) {
            System.out.println("✗ Erreur inattendue: " + e.getMessage());
        }
        System.out.println();
    }

    //TD6 -------------------------------------------------------------------------------------------------------------

    private static void testTD6_UnitConversion(DataRetriever dr) {
        System.out.println("--- Test TD6: Conversions d'unités ---");

        try {
            // RÉINITIALISER LA BASE AVANT LE TEST
            System.out.println("\n⚙️  Réinitialisation des stocks...");
            dr.resetStocksForTesting();
            System.out.println();

            List<Ingredient> allIngredients = dr.findIngredients(1, 100);

            // === ÉTAPE 1: Vérifier le stock initial ===
            System.out.println("=== STOCK INITIAL (au 2024-01-01) ===");

            Instant stockInitialDate = Instant.parse("2024-01-01T00:00:00Z");

            System.out.println("\nIngrédient          Stock (KG)");
            System.out.println("-----------------------------------");

            String[] ingredients = {"Laitue", "Tomate", "Poulet", "Chocolat", "Beurre"};

            for (String ingName : ingredients) {
                Ingredient ing = allIngredients.stream()
                        .filter(x -> x.getName().equals(ingName))
                        .findFirst().orElse(null);

                if (ing != null) {
                    Stock stock = dr.getStockValueAt(ing.getId(), stockInitialDate);
                    System.out.printf("%-20s %.1f%n", ingName, stock.getQuantity());
                }
            }

            // === ÉTAPE 2: Ajouter les mouvements OUT ===
            System.out.println("\n=== NOUVEAUX MOUVEMENTS (Sorties de stock) ===\n");

            Instant mouvementDate = Instant.parse("2024-01-05T10:00:00Z");

            System.out.println("Ingrédient   Quantité   Unité   Type   Commentaire");
            System.out.println("-------------------------------------------------------");

            // Tomate: -5 PCS
            Ingredient tomate = allIngredients.stream()
                    .filter(i -> i.getName().equals("Tomate")).findFirst().orElse(null);
            if (tomate != null) {
                StockMovement sm = new StockMovement();
                sm.setQuantity(-5.0);
                sm.setUnit("PCS");
                sm.setMovementDate(mouvementDate);
                tomate.setStockMovementList(List.of(sm));
                dr.saveIngredient(tomate);
                System.out.println("Tomate       -5         PCS     OUT    Préparation salade");
            }

            // Laitue: -2 PCS
            Ingredient laitue = allIngredients.stream()
                    .filter(i -> i.getName().equals("Laitue")).findFirst().orElse(null);
            if (laitue != null) {
                StockMovement sm = new StockMovement();
                sm.setQuantity(-2.0);
                sm.setUnit("PCS");
                sm.setMovementDate(mouvementDate);
                laitue.setStockMovementList(List.of(sm));
                dr.saveIngredient(laitue);
                System.out.println("Laitue       -2         PCS     OUT    Préparation salade");
            }

            // Chocolat: -1 L
            Ingredient chocolat = allIngredients.stream()
                    .filter(i -> i.getName().equals("Chocolat")).findFirst().orElse(null);
            if (chocolat != null) {
                StockMovement sm = new StockMovement();
                sm.setQuantity(-1.0);
                sm.setUnit("L");
                sm.setMovementDate(mouvementDate);
                chocolat.setStockMovementList(List.of(sm));
                dr.saveIngredient(chocolat);
                System.out.println("Chocolat     -1         L       OUT    Dessert");
            }

            // Poulet: -4 PCS
            Ingredient poulet = allIngredients.stream()
                    .filter(i -> i.getName().equals("Poulet")).findFirst().orElse(null);
            if (poulet != null) {
                StockMovement sm = new StockMovement();
                sm.setQuantity(-4.0);
                sm.setUnit("PCS");
                sm.setMovementDate(mouvementDate);
                poulet.setStockMovementList(List.of(sm));
                dr.saveIngredient(poulet);
                System.out.println("Poulet       -4         PCS     OUT    Plat principal");
            }

            // Beurre: -1 L
            Ingredient beurre = allIngredients.stream()
                    .filter(i -> i.getName().equals("Beurre")).findFirst().orElse(null);
            if (beurre != null) {
                StockMovement sm = new StockMovement();
                sm.setQuantity(-1.0);
                sm.setUnit("L");
                sm.setMovementDate(mouvementDate);
                beurre.setStockMovementList(List.of(sm));
                dr.saveIngredient(beurre);
                System.out.println("Beurre       -1         L       OUT    Pâtisserie");
            }

            // === ÉTAPE 3: Vérifier le stock final ===
            System.out.println("\n=== RÉSULTAT DU STOCK ATTENDU ===\n");

            Instant stockFinalDate = Instant.parse("2024-01-06T00:00:00Z");

            System.out.println("Ingrédient    Stock avant    Sortie    Stock final    Attendu    Résultat");
            System.out.println("-----------------------------------------------------------------------------");

            // Résultats attendus
            Object[][] resultatsAttendus = {
                    {"Laitue",   5.0,  1.0,  4.0},
                    {"Tomate",   4.0,  0.5,  3.5},
                    {"Poulet",  10.0,  0.5,  9.5},
                    {"Chocolat", 3.0,  0.4,  2.6},
                    {"Beurre",   2.5,  0.2,  2.3}
            };

            boolean tousCorrects = true;

            for (Object[] row : resultatsAttendus) {
                String nom = (String) row[0];
                double stockAvant = (Double) row[1];
                double sortie = (Double) row[2];
                double stockAttendu = (Double) row[3];

                Ingredient ing = allIngredients.stream()
                        .filter(x -> x.getName().equals(nom))
                        .findFirst().orElse(null);

                if (ing != null) {
                    Stock stockFinal = dr.getStockValueAt(ing.getId(), stockFinalDate);
                    double stockCalcule = stockFinal.getQuantity();
                    boolean correct = Math.abs(stockCalcule - stockAttendu) < 0.01;

                    System.out.printf("%-13s %.1f          %.1f         %.1f          %.1f      %s%n",
                            nom,
                            stockAvant,
                            sortie,
                            stockCalcule,
                            stockAttendu,
                            correct ? "✓" : "✗"
                    );

                    if (!correct) {
                        tousCorrects = false;
                    }
                }
            }

            System.out.println("\n" + (tousCorrects ? "✓ TOUS LES CALCULS SONT CORRECTS!" : "✗ Certains calculs sont incorrects"));

        } catch (Exception e) {
            System.out.println("\n✗ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }


    //TESTS EXAMEN---------------------------------------------------------------------------------------------------------------------------------------
    private static void testEval_CreateOrderWithTable(DataRetriever dr) {
        System.out.println("--- Test EVAL.1: Créer une commande avec table ---");
        try {
            Order newOrder = new Order();
            newOrder.setCreationDatetime(Instant.now());

            // Spécifier la table
            RestaurantTable table = new RestaurantTable(1, 1);
            newOrder.setTable(table);

            // Spécifier les dates d'arrivée et départ
            newOrder.setArrivalDatetime(Instant.parse("2026-01-30T12:00:00Z"));
            newOrder.setDepartureDatetime(Instant.parse("2026-01-30T14:00:00Z"));

            List<DishOrder> dishOrders = new ArrayList<>();
            Dish dish1 = dr.findDishById(1);
            DishOrder do1 = new DishOrder();
            do1.setDish(dish1);
            do1.setQuantity(1);
            dishOrders.add(do1);

            newOrder.setDishOrders(dishOrders);

            Order saved = dr.saveOrder(newOrder);
            System.out.println("✓ Commande créée: " + saved.getReference());
            System.out.println("  Table: n°" + saved.getTable().getTableNumber());
            System.out.println("  Arrivée: " + saved.getArrivalDatetime());
            System.out.println("  Départ: " + saved.getDepartureDatetime());

        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testEval_TableNotAvailable(DataRetriever dr) {
        System.out.println("--- Test EVAL.2: Table occupée mais autres disponibles ---");
        try {
            Order newOrder = new Order();
            newOrder.setCreationDatetime(Instant.now());

            // Essayer d'utiliser la même table au même moment
            RestaurantTable table = new RestaurantTable(1, 1);
            newOrder.setTable(table);

            newOrder.setArrivalDatetime(Instant.parse("2026-01-30T12:30:00Z"));
            newOrder.setDepartureDatetime(Instant.parse("2026-01-30T13:30:00Z"));

            List<DishOrder> dishOrders = new ArrayList<>();
            Dish dish1 = dr.findDishById(1);
            DishOrder do1 = new DishOrder();
            do1.setDish(dish1);
            do1.setQuantity(1);
            dishOrders.add(do1);

            newOrder.setDishOrders(dishOrders);

            dr.saveOrder(newOrder);
            System.out.println("✗ FAIL: Devrait lever une exception");

        } catch (RuntimeException e) {
            System.out.println("✓ Exception levée (attendu):");
            System.out.println("  " + e.getMessage());
        } catch (Exception e) {
            System.out.println("✗ Erreur inattendue: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testEval_NoTableAvailable(DataRetriever dr) {
        System.out.println("--- Test EVAL.3: Aucune table disponible ---");
        try {
            // Créer des commandes sur toutes les tables
            for (int i = 2; i <= 5; i++) {
                Order order = new Order();
                order.setCreationDatetime(Instant.now());

                RestaurantTable table = new RestaurantTable(i, i);
                order.setTable(table);

                order.setArrivalDatetime(Instant.parse("2026-01-30T18:00:00Z"));
                order.setDepartureDatetime(Instant.parse("2026-01-30T20:00:00Z"));

                List<DishOrder> dishOrders = new ArrayList<>();
                Dish dish1 = dr.findDishById(1);
                DishOrder do1 = new DishOrder();
                do1.setDish(dish1);
                do1.setQuantity(1);
                dishOrders.add(do1);

                order.setDishOrders(dishOrders);

                dr.saveOrder(order);
            }

            // Maintenant essayer de créer une commande alors que toutes les tables sont prises
            Order newOrder = new Order();
            newOrder.setCreationDatetime(Instant.now());

            RestaurantTable table = new RestaurantTable(1, 1);
            newOrder.setTable(table);

            newOrder.setArrivalDatetime(Instant.parse("2026-01-30T18:30:00Z"));
            newOrder.setDepartureDatetime(Instant.parse("2026-01-30T19:30:00Z"));

            List<DishOrder> dishOrders = new ArrayList<>();
            Dish dish1 = dr.findDishById(1);
            DishOrder do1 = new DishOrder();
            do1.setDish(dish1);
            do1.setQuantity(1);
            dishOrders.add(do1);

            newOrder.setDishOrders(dishOrders);

            dr.saveOrder(newOrder);
            System.out.println("✗ FAIL: Devrait lever une exception");

        } catch (RuntimeException e) {
            System.out.println("✓ Exception levée (attendu):");
            System.out.println("  " + e.getMessage());
        } catch (Exception e) {
            System.out.println("✗ Erreur inattendue: " + e.getMessage());
        }
        System.out.println();
    }
}