CREATE TABLE restaurant_table (
    id SERIAL PRIMARY KEY,
    table_number INT NOT NULL UNIQUE
);

ALTER TABLE "order" ADD COLUMN id_table INT REFERENCES restaurant_table(id);
ALTER TABLE "order" ADD COLUMN arrival_datetime TIMESTAMP;
ALTER TABLE "order" ADD COLUMN departure_datetime TIMESTAMP;

INSERT INTO restaurant_table (table_number) VALUES (1), (2), (3), (4), (5)
ON CONFLICT (table_number) DO NOTHING;