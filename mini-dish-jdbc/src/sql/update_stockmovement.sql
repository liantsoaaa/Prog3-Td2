-- table stock_movement
CREATE TABLE stock_movement (
    id SERIAL PRIMARY KEY,
    id_ingredient INT NOT NULL REFERENCES ingredient(id),
    quantity DOUBLE PRECISION NOT NULL,
    unit VARCHAR(10) NOT NULL,
    movement_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_ingredient FOREIGN KEY (id_ingredient) REFERENCES ingredient(id)
);

-- stock initial
INSERT INTO stock_movement (id_ingredient, quantity, unit, movement_date) VALUES
((SELECT id FROM ingredient WHERE name = 'Tomate'), 50.0, 'KG', '2024-01-01 00:00:00'),
((SELECT id FROM ingredient WHERE name = 'Laitue'), 30.0, 'KG', '2024-01-01 00:00:00'),
((SELECT id FROM ingredient WHERE name = 'Poulet'), 40.0, 'KG', '2024-01-01 00:00:00'),
((SELECT id FROM ingredient WHERE name = 'Chocolat'), 25.0, 'KG', '2024-01-01 00:00:00'),
((SELECT id FROM ingredient WHERE name = 'Beurre'), 20.0, 'KG', '2024-01-01 00:00:00'),
((SELECT id FROM ingredient WHERE name = 'Fromage'), 35.0, 'KG', '2024-01-01 00:00:00');

-- Ajouter les mouvements de stock de test (achats et ventes)
INSERT INTO stock_movement (id_ingredient, quantity, unit, movement_date) VALUES
-- Achats
((SELECT id FROM ingredient WHERE name = 'Tomate'), 20.0, 'KG', '2024-01-03 10:00:00'),
((SELECT id FROM ingredient WHERE name = 'Poulet'), 15.0, 'KG', '2024-01-04 14:00:00'),

-- Ventes
((SELECT id FROM ingredient WHERE name = 'Tomate'), -10.0, 'KG', '2024-01-05 12:00:00'),
((SELECT id FROM ingredient WHERE name = 'Chocolat'), -5.0, 'KG', '2024-01-05 15:00:00'),
((SELECT id FROM ingredient WHERE name = 'Poulet'), -8.0, 'KG', '2024-01-06 09:00:00');