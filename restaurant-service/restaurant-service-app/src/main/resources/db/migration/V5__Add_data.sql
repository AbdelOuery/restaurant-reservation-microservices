INSERT INTO restaurant (name, address, phone, email, is_closed, created_at, updated_at) VALUES
('Pizza Palace', '123 Main Street, Paris', '+33123456789', 'info@pizzapalace.fr', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Burger Barn', '456 Oak Avenue, Paris', '+33987654321', 'contact@burgerbarn.fr', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sushi Studio', '789 Elm Road, Paris', '+33555555555', 'hello@sushistudio.fr', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Tables for Restaurant 1 (different capacities)
INSERT INTO tables (restaurant_id, table_number, capacity, created_at, updated_at) VALUES
(1, 'T1', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'T2', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'T3', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'T4', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Tables for Restaurant 2
INSERT INTO tables (restaurant_id, table_number, capacity, created_at, updated_at) VALUES
(2, 'A1', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'A2', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'A3', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Tables for Restaurant 3
INSERT INTO tables (restaurant_id, table_number, capacity, created_at, updated_at) VALUES
(3, '1', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '2', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '3', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);