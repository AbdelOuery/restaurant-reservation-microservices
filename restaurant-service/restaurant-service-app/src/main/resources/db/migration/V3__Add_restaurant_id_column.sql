ALTER TABLE tables ADD COLUMN restaurant_id BIGINT;

ALTER TABLE tables
ADD CONSTRAINT fk_tables_restaurant
    FOREIGN KEY (restaurant_id)
    REFERENCES restaurant(id);