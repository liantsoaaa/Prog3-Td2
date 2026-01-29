-- tables
CREATE TABLE "order" (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(255) UNIQUE NOT NULL,
    creation_datetime TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE dish_order (
    id SERIAL PRIMARY KEY,
    id_order INT NOT NULL REFERENCES "order"(id),
    id_dish INT NOT NULL REFERENCES dish(id),
    quantity INT NOT NULL CHECK (quantity > 0)
);

-- Ajoute de price dans Dish
ALTER TABLE dish ADD COLUMN IF NOT EXISTS price NUMERIC(10, 2);

-- Creation de la fonction
CREATE FUNCTION generate_order_reference()
RETURNS VARCHAR AS $$
DECLARE
next_number INT;
    new_reference VARCHAR(50);
BEGIN
-- Récupérer le nombre actuel de commandes
SELECT COUNT(*) + 1 INTO next_number FROM "order";

-- Générer la référence au format ORDXXXXX
new_reference := 'ORD' || LPAD(next_number::TEXT, 5, '0');

RETURN new_reference;
END;
$$ LANGUAGE plpgsql;


