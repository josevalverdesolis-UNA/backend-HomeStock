-- V3 (opcional): Datos de ejemplo idempotentes
-- Inserta 2–3 filas por tabla clave. Usa ON CONFLICT/WHERE NOT EXISTS para evitar duplicados.

SET search_path TO public;

-- Users
INSERT INTO users (name, email)
SELECT 'Alice', 'alice@example.com'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'alice@example.com');

INSERT INTO users (name, email)
SELECT 'Bob', 'bob@example.com'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'bob@example.com');

-- Categories
INSERT INTO categories (name, description)
SELECT 'Pantry', 'Non-perishable pantry items'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Pantry');

INSERT INTO categories (name, description)
SELECT 'Beverages', 'Drinks and beverages'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Beverages');

-- Stores
INSERT INTO stores (name, location, notes)
SELECT 'SuperMart', 'Main St 123', 'Large supermarket'
WHERE NOT EXISTS (SELECT 1 FROM stores WHERE name = 'SuperMart');

INSERT INTO stores (name, location, notes)
SELECT 'Local Market', '2nd Ave 45', 'Neighborhood store'
WHERE NOT EXISTS (SELECT 1 FROM stores WHERE name = 'Local Market');

-- Products for Alice
INSERT INTO products (
  user_id, name, category_id, quantity, min_stock, expiry_date, price, purchase_location_id, brand, image_url
)
SELECT u.id, 'Rice 1kg', c.id, 5, 2, NULL, 2.5000, s.id, 'Generic', NULL
FROM users u, categories c, stores s
WHERE u.email = 'alice@example.com' AND c.name = 'Pantry' AND s.name = 'SuperMart'
  AND NOT EXISTS (
    SELECT 1 FROM products p WHERE p.user_id = u.id AND p.name = 'Rice 1kg'
  );

INSERT INTO products (
  user_id, name, category_id, quantity, min_stock, expiry_date, price, purchase_location_id, brand, image_url
)
SELECT u.id, 'Coffee 250g', c.id, 1, 1, NULL, 4.9900, s.id, 'Acme', NULL
FROM users u, categories c, stores s
WHERE u.email = 'alice@example.com' AND c.name = 'Beverages' AND s.name = 'Local Market'
  AND NOT EXISTS (
    SELECT 1 FROM products p WHERE p.user_id = u.id AND p.name = 'Coffee 250g'
  );

-- Product for Bob
INSERT INTO products (
  user_id, name, category_id, quantity, min_stock, expiry_date, price, purchase_location_id, brand, image_url
)
SELECT u.id, 'Pasta 500g', c.id, 3, 1, NULL, 1.7500, s.id, 'Italiano', NULL
FROM users u, categories c, stores s
WHERE u.email = 'bob@example.com' AND c.name = 'Pantry' AND s.name = 'SuperMart'
  AND NOT EXISTS (
    SELECT 1 FROM products p WHERE p.user_id = u.id AND p.name = 'Pasta 500g'
  );

-- Movements (purchases add stock)
INSERT INTO movements (user_id, product_id, type, quantity, unit_price, store_id, note)
SELECT u.id, p.id, 'PURCHASE', 5, 2.5000, s.id, 'Initial stock'
FROM users u
JOIN products p ON p.name = 'Rice 1kg' AND p.user_id = u.id
JOIN stores s ON s.name = 'SuperMart'
WHERE u.email = 'alice@example.com'
  AND NOT EXISTS (
    SELECT 1 FROM movements m WHERE m.product_id = p.id AND m.type = 'PURCHASE' AND m.quantity = 5
  );

INSERT INTO movements (user_id, product_id, type, quantity, unit_price, store_id, note)
SELECT u.id, p.id, 'PURCHASE', 1, 4.9900, s.id, 'Coffee buy'
FROM users u
JOIN products p ON p.name = 'Coffee 250g' AND p.user_id = u.id
JOIN stores s ON s.name = 'Local Market'
WHERE u.email = 'alice@example.com'
  AND NOT EXISTS (
    SELECT 1 FROM movements m WHERE m.product_id = p.id AND m.type = 'PURCHASE' AND m.quantity = 1
  );

-- Shopping items
INSERT INTO shopping_items (user_id, product_id, quantity, is_purchased, purchased_at, source)
SELECT u.id, p.id, 1, FALSE, NULL, 'MANUAL'
FROM users u
JOIN products p ON p.name = 'Pasta 500g' AND p.user_id = u.id
WHERE u.email = 'bob@example.com'
  AND NOT EXISTS (
    SELECT 1 FROM shopping_items si WHERE si.user_id = u.id AND si.product_id = p.id AND si.is_purchased = FALSE
  );

-- Alerts (one product alert and one general for Alice)
INSERT INTO alerts (user_id, product_id, type, message, trigger_at, is_active)
SELECT u.id, p.id, 'LOW_STOCK', 'Low stock for coffee', now(), TRUE
FROM users u
JOIN products p ON p.name = 'Coffee 250g' AND p.user_id = u.id
WHERE u.email = 'alice@example.com'
  AND NOT EXISTS (
    SELECT 1 FROM alerts a WHERE a.user_id = u.id AND a.product_id = p.id AND a.type = 'LOW_STOCK'
  );

INSERT INTO alerts (user_id, product_id, type, message, trigger_at, is_active)
SELECT u.id, NULL, 'EXPIRY', 'Check expiry dates', now(), TRUE
FROM users u
WHERE u.email = 'alice@example.com'
  AND NOT EXISTS (
    SELECT 1 FROM alerts a WHERE a.user_id = u.id AND a.product_id IS NULL AND a.type = 'EXPIRY'
  );

-- Price history
INSERT INTO price_history (product_id, unit_price, store_id, recorded_at)
SELECT p.id, 2.4000, s.id, now()
FROM products p
JOIN users u ON u.id = p.user_id AND u.email = 'alice@example.com'
JOIN stores s ON s.name = 'SuperMart'
WHERE p.name = 'Rice 1kg'
  AND NOT EXISTS (
    SELECT 1 FROM price_history ph WHERE ph.product_id = p.id AND ph.unit_price = 2.4000
  );

INSERT INTO price_history (product_id, unit_price, store_id, recorded_at)
SELECT p.id, 4.9900, s.id, now()
FROM products p
JOIN users u ON u.id = p.user_id AND u.email = 'alice@example.com'
JOIN stores s ON s.name = 'Local Market'
WHERE p.name = 'Coffee 250g'
  AND NOT EXISTS (
    SELECT 1 FROM price_history ph WHERE ph.product_id = p.id AND ph.unit_price = 4.9900
  );

-- Product ratings
INSERT INTO product_ratings (user_id, product_id, quality_score, notes)
SELECT u.id, p.id, 5, 'Great quality'
FROM users u
JOIN products p ON p.name = 'Rice 1kg' AND p.user_id = u.id
WHERE u.email = 'alice@example.com'
  AND NOT EXISTS (
    SELECT 1 FROM product_ratings r WHERE r.user_id = u.id AND r.product_id = p.id
  );

INSERT INTO product_ratings (user_id, product_id, quality_score, notes)
SELECT u.id, p.id, 4, 'Good coffee'
FROM users u
JOIN products p ON p.name = 'Coffee 250g' AND p.user_id = u.id
WHERE u.email = 'alice@example.com'
  AND NOT EXISTS (
    SELECT 1 FROM product_ratings r WHERE r.user_id = u.id AND r.product_id = p.id
  );

