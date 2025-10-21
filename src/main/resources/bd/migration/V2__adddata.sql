-- HomeStock – Super Complete Test Data (for unified schema)
-- Safe to run multiple times (idempotent). PostgreSQL 14+.

-- Nota: no fijamos search_path; Flyway ya define current_schema según config

-- =========================
-- Users (explicit hashes & roles)
-- =========================
DO $$
DECLARE has_pgcrypto boolean;
BEGIN
  SELECT EXISTS(SELECT 1 FROM pg_catalog.pg_extension WHERE extname='pgcrypto') INTO has_pgcrypto;

  IF has_pgcrypto THEN
    -- Inserta con contraseñas bcrypt generadas por pgcrypto (crypt + gen_salt('bf', 12))
    INSERT INTO users (name, email, password_hash, role, created_at, updated_at)
    VALUES ('Alice', 'alice@example.com', crypt('Alice123!', gen_salt('bf', 12)), 'USER', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00')
    ON CONFLICT (email) DO NOTHING;

    INSERT INTO users (name, email, password_hash, role, created_at, updated_at)
    VALUES ('Bob', 'bob@example.com', crypt('Bob123!', gen_salt('bf', 12)), 'USER', '2025-01-06 10:00:00+00', '2025-01-06 10:00:00+00')
    ON CONFLICT (email) DO NOTHING;

    INSERT INTO users (name, email, password_hash, role, created_at, updated_at)
    VALUES ('Admin', 'admin@homestock.test', crypt('Adm1n$tr0ng!', gen_salt('bf', 12)), 'ADMIN', '2025-01-07 11:00:00+00', '2025-01-07 11:00:00+00')
    ON CONFLICT (email) DO NOTHING;
  ELSE
    -- Fallback sin pgcrypto: crea usuarios con password_hash vacío. Podrás registrar/loguear usuarios reales vía API.
    INSERT INTO users (name, email, password_hash, role, created_at, updated_at)
    VALUES ('Alice', 'alice@example.com', '', 'USER', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00')
    ON CONFLICT (email) DO NOTHING;

    INSERT INTO users (name, email, password_hash, role, created_at, updated_at)
    VALUES ('Bob', 'bob@example.com', '', 'USER', '2025-01-06 10:00:00+00', '2025-01-06 10:00:00+00')
    ON CONFLICT (email) DO NOTHING;

    INSERT INTO users (name, email, password_hash, role, created_at, updated_at)
    VALUES ('Admin', 'admin@homestock.test', '', 'ADMIN', '2025-01-07 11:00:00+00', '2025-01-07 11:00:00+00')
    ON CONFLICT (email) DO NOTHING;
  END IF;
END $$;

-- =========================
-- Categories
-- =========================
INSERT INTO categories (name, description, created_at, updated_at)
VALUES 
  ('Pantry', 'Non-perishable pantry items', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'),
  ('Beverages', 'Drinks and beverages', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'),
  ('Dairy', 'Milk and dairy products', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'),
  ('Cleaning', 'House cleaning supplies', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00')
ON CONFLICT (name) DO NOTHING;

-- =========================
-- Stores
-- =========================
INSERT INTO stores (name, location, notes, created_at, updated_at)
VALUES
  ('SuperMart', 'Main St 123', 'Large supermarket', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'),
  ('Local Market', '2nd Ave 45', 'Neighborhood store', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'),
  ('Online Store', 'Webshop', 'E-commerce vendor', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00')
ON CONFLICT (name, location) DO NOTHING;

-- =========================
-- Products (explicit all fields)
-- =========================
WITH u AS (
  SELECT id FROM users WHERE email = 'alice@example.com'
), b AS (
  SELECT id FROM users WHERE email = 'bob@example.com'
), a AS (
  SELECT id FROM users WHERE email = 'admin@homestock.test'
), c AS (
  SELECT id FROM categories WHERE name = 'Pantry'
), cb AS (
  SELECT id FROM categories WHERE name = 'Beverages'
), cd AS (
  SELECT id FROM categories WHERE name = 'Dairy'
), cc AS (
  SELECT id FROM categories WHERE name = 'Cleaning'
), s1 AS (
  SELECT id FROM stores WHERE name='SuperMart' AND location='Main St 123'
), s2 AS (
  SELECT id FROM stores WHERE name='Local Market' AND location='2nd Ave 45'
), s3 AS (
  SELECT id FROM stores WHERE name='Online Store' AND location='Webshop'
)
INSERT INTO products (user_id, name, category_id, quantity, min_stock, expiry_date, price, purchase_location_id, brand, image_url, acquisition_date, created_at, updated_at)
SELECT u.id, 'Rice 1kg', c.id, 5, 2, NULL, 2.50, s1.id, 'Generic', 'https://example.com/img/rice1kg.png', '2025-01-02', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'
FROM u, c, s1
ON CONFLICT (user_id, name) DO NOTHING;

INSERT INTO products (user_id, name, category_id, quantity, min_stock, expiry_date, price, purchase_location_id, brand, image_url, acquisition_date, created_at, updated_at)
SELECT u.id, 'Coffee 250g', cb.id, 1, 2, NULL, 4.99, s2.id, 'Acme', 'https://example.com/img/coffee250.png', '2025-01-03', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'
FROM u, cb, s2
ON CONFLICT (user_id, name) DO NOTHING;

INSERT INTO products (user_id, name, category_id, quantity, min_stock, expiry_date, price, purchase_location_id, brand, image_url, acquisition_date, created_at, updated_at)
SELECT b.id, 'Pasta 500g', c.id, 3, 1, NULL, 1.75, s1.id, 'Italiano', 'https://example.com/img/pasta500.png', '2025-01-04', '2025-01-06 10:00:00+00', '2025-01-06 10:00:00+00'
FROM b, c, s1
ON CONFLICT (user_id, name) DO NOTHING;

INSERT INTO products (user_id, name, category_id, quantity, min_stock, expiry_date, price, purchase_location_id, brand, image_url, acquisition_date, created_at, updated_at)
SELECT u.id, 'Milk 1L', cd.id, 2, 2, '2025-11-01', 1.25, s1.id, 'FarmFresh', 'https://example.com/img/milk1l.png', '2025-01-04', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'
FROM u, cd, s1
ON CONFLICT (user_id, name) DO NOTHING;

INSERT INTO products (user_id, name, category_id, quantity, min_stock, expiry_date, price, purchase_location_id, brand, image_url, acquisition_date, created_at, updated_at)
SELECT a.id, 'Bleach 2L', cc.id, 10, 3, NULL, 3.10, s3.id, 'CleanCo', 'https://example.com/img/bleach2l.png', '2025-01-02', '2025-01-07 11:00:00+00', '2025-01-07 11:00:00+00'
FROM a, cc, s3
ON CONFLICT (user_id, name) DO NOTHING;

-- =========================
-- Movements (PURCHASE, CONSUMPTION, ADJUSTMENT) with explicit timestamps
-- =========================
WITH u AS (SELECT id FROM users WHERE email='alice@example.com'),
     b AS (SELECT id FROM users WHERE email='bob@example.com'),
     s1 AS (SELECT id FROM stores WHERE name='SuperMart' AND location='Main St 123'),
     s2 AS (SELECT id FROM stores WHERE name='Local Market' AND location='2nd Ave 45'),
     s3 AS (SELECT id FROM stores WHERE name='Online Store' AND location='Webshop'),
     pr_rice AS (SELECT id, user_id FROM products WHERE name='Rice 1kg'),
     pr_coffee AS (SELECT id, user_id FROM products WHERE name='Coffee 250g'),
     pr_pasta AS (SELECT id, user_id FROM products WHERE name='Pasta 500g'),
     pr_milk AS (SELECT id, user_id FROM products WHERE name='Milk 1L'),
     pr_bleach AS (SELECT id, user_id FROM products WHERE name='Bleach 2L')
-- Purchases
INSERT INTO movements (user_id, product_id, type, quantity, unit_price, store_id, note, occurred_at, created_at, updated_at)
SELECT u.id, pr_rice.id, 'PURCHASE', 5, 2.40, s1.id, 'Initial stock', '2025-01-05 10:00:00+00', '2025-01-05 10:00:00+00', '2025-01-05 10:00:00+00'
FROM u, pr_rice, s1
WHERE pr_rice.user_id = u.id
ON CONFLICT DO NOTHING;

INSERT INTO movements (user_id, product_id, type, quantity, unit_price, store_id, note, occurred_at, created_at, updated_at)
SELECT u.id, pr_coffee.id, 'PURCHASE', 2, 4.80, s2.id, 'Promo pack', '2025-01-06 12:00:00+00', '2025-01-06 12:00:00+00', '2025-01-06 12:00:00+00'
FROM u, pr_coffee, s2
WHERE pr_coffee.user_id = u.id
ON CONFLICT DO NOTHING;

INSERT INTO movements (user_id, product_id, type, quantity, unit_price, store_id, note, occurred_at, created_at, updated_at)
SELECT b.id, pr_pasta.id, 'PURCHASE', 3, 1.60, s1.id, 'Weekly stock', '2025-01-06 15:00:00+00', '2025-01-06 15:00:00+00', '2025-01-06 15:00:00+00'
FROM b, pr_pasta, s1
WHERE pr_pasta.user_id = b.id
ON CONFLICT DO NOTHING;

-- Consumptions
INSERT INTO movements (user_id, product_id, type, quantity, unit_price, store_id, note, occurred_at, created_at, updated_at)
SELECT u.id, pr_rice.id, 'CONSUMPTION', 1, NULL, NULL, 'Used for lunch', '2025-01-07 13:00:00+00', '2025-01-07 13:00:00+00', '2025-01-07 13:00:00+00'
FROM u, pr_rice
WHERE pr_rice.user_id = u.id
ON CONFLICT DO NOTHING;

INSERT INTO movements (user_id, product_id, type, quantity, unit_price, store_id, note, occurred_at, created_at, updated_at)
SELECT u.id, pr_coffee.id, 'CONSUMPTION', 1, NULL, NULL, 'Morning brew', '2025-01-08 07:30:00+00', '2025-01-08 07:30:00+00', '2025-01-08 07:30:00+00'
FROM u, pr_coffee
WHERE pr_coffee.user_id = u.id
ON CONFLICT DO NOTHING;

-- Adjustments (e.g., inventory recount)
INSERT INTO movements (user_id, product_id, type, quantity, unit_price, store_id, note, occurred_at, created_at, updated_at)
SELECT u.id, pr_milk.id, 'ADJUSTMENT', 1, 1.30, s1.id, 'Damaged item replaced', '2025-01-08 10:00:00+00', '2025-01-08 10:00:00+00', '2025-01-08 10:00:00+00'
FROM u, pr_milk, s1
WHERE pr_milk.user_id = u.id
ON CONFLICT DO NOTHING;

INSERT INTO movements (user_id, product_id, type, quantity, unit_price, store_id, note, occurred_at, created_at, updated_at)
SELECT u.id, pr_bleach.id, 'ADJUSTMENT', 2, 3.00, s3.id, 'Count correction', '2025-01-09 09:00:00+00', '2025-01-09 09:00:00+00', '2025-01-09 09:00:00+00'
FROM u, pr_bleach, s3
WHERE pr_bleach.user_id = u.id
ON CONFLICT DO NOTHING;

-- =========================
-- Shopping Items (MANUAL & AUTO_RULE; purchased and not purchased)
-- =========================
WITH u AS (SELECT id FROM users WHERE email='alice@example.com'),
     b AS (SELECT id FROM users WHERE email='bob@example.com'),
     s1 AS (SELECT id FROM stores WHERE name='SuperMart' AND location='Main St 123'),
     s2 AS (SELECT id FROM stores WHERE name='Local Market' AND location='2nd Ave 45'),
     pr_coffee AS (SELECT id, user_id FROM products WHERE name='Coffee 250g'),
     pr_pasta AS (SELECT id, user_id FROM products WHERE name='Pasta 500g')
-- Alice manual, not purchased
INSERT INTO shopping_items (user_id, product_id, desired_quantity, is_purchased, purchased_at, source, target_store_id, created_at, updated_at)
SELECT u.id, pr_coffee.id, 2, FALSE, NULL, 'MANUAL', s2.id, '2025-01-08 08:00:00+00', '2025-01-08 08:00:00+00'
FROM u, pr_coffee, s2
WHERE pr_coffee.user_id = u.id
ON CONFLICT (user_id, product_id, is_purchased) DO NOTHING;

-- Bob auto_rule, purchased already
INSERT INTO shopping_items (user_id, product_id, desired_quantity, is_purchased, purchased_at, source, target_store_id, created_at, updated_at)
SELECT b.id, pr_pasta.id, 1, TRUE, '2025-01-10 16:30:00+00', 'AUTO_RULE', s1.id, '2025-01-10 16:00:00+00', '2025-01-10 16:30:00+00'
FROM b, pr_pasta, s1
WHERE pr_pasta.user_id = b.id
ON CONFLICT (user_id, product_id, is_purchased) DO NOTHING;

-- =========================
-- Alerts (LOW_STOCK & EXPIRY)
-- =========================
WITH u AS (SELECT id FROM users WHERE email='alice@example.com'),
     pr_coffee AS (SELECT id, user_id FROM products WHERE name='Coffee 250g'),
     pr_milk AS (SELECT id, user_id FROM products WHERE name='Milk 1L')
-- Low stock on coffee
INSERT INTO alerts (user_id, product_id, type, message, trigger_at, is_active, resolved_at, created_at, updated_at)
SELECT u.id, pr_coffee.id, 'LOW_STOCK', 'Coffee below min stock', '2025-01-08 08:05:00+00', TRUE, NULL, '2025-01-08 08:05:00+00', '2025-01-08 08:05:00+00'
FROM u, pr_coffee
WHERE pr_coffee.user_id = u.id
ON CONFLICT DO NOTHING;

-- Expiry check (general)
INSERT INTO alerts (user_id, product_id, type, message, trigger_at, is_active, resolved_at, created_at, updated_at)
SELECT u.id, NULL, 'EXPIRY', 'Check soon-to-expire items', '2025-01-08 08:10:00+00', TRUE, NULL, '2025-01-08 08:10:00+00', '2025-01-08 08:10:00+00'
FROM u
ON CONFLICT DO NOTHING;

-- =========================
-- Price History (multiple entries, multiple stores)
-- =========================
WITH pr_rice AS (SELECT id FROM products WHERE name='Rice 1kg'),
     pr_coffee AS (SELECT id FROM products WHERE name='Coffee 250g'),
     s1 AS (SELECT id FROM stores WHERE name='SuperMart' AND location='Main St 123'),
     s2 AS (SELECT id FROM stores WHERE name='Local Market' AND location='2nd Ave 45')
INSERT INTO price_history (product_id, unit_price, store_id, recorded_at, created_at, updated_at)
SELECT pr_rice.id, 2.40, s1.id, '2025-01-04 12:00:00+00', '2025-01-04 12:00:00+00', '2025-01-04 12:00:00+00' FROM pr_rice, s1
ON CONFLICT DO NOTHING;
INSERT INTO price_history (product_id, unit_price, store_id, recorded_at, created_at, updated_at)
SELECT pr_rice.id, 2.55, s1.id, '2025-02-01 12:00:00+00', '2025-02-01 12:00:00+00', '2025-02-01 12:00:00+00' FROM pr_rice, s1
ON CONFLICT DO NOTHING;

INSERT INTO price_history (product_id, unit_price, store_id, recorded_at, created_at, updated_at)
SELECT pr_coffee.id, 4.80, s2.id, '2025-01-05 12:00:00+00', '2025-01-05 12:00:00+00', '2025-01-05 12:00:00+00' FROM pr_coffee, s2
ON CONFLICT DO NOTHING;
INSERT INTO price_history (product_id, unit_price, store_id, recorded_at, created_at, updated_at)
SELECT pr_coffee.id, 5.10, s2.id, '2025-02-07 12:00:00+00', '2025-02-07 12:00:00+00', '2025-02-07 12:00:00+00' FROM pr_coffee, s2
ON CONFLICT DO NOTHING;

-- =========================
-- Product Ratings (1..5 scale)
-- =========================
WITH u AS (SELECT id FROM users WHERE email='alice@example.com'),
     b AS (SELECT id FROM users WHERE email='bob@example.com'),
     pr_rice AS (SELECT id FROM products WHERE name='Rice 1kg'),
     pr_coffee AS (SELECT id FROM products WHERE name='Coffee 250g'),
     pr_pasta AS (SELECT id FROM products WHERE name='Pasta 500g'),
     pr_milk AS (SELECT id FROM products WHERE name='Milk 1L')
INSERT INTO product_ratings (user_id, product_id, quality_score, notes, created_at, updated_at)
SELECT u.id, pr_rice.id, 5, 'Excellent quality rice', '2025-01-09 10:00:00+00', '2025-01-09 10:00:00+00' FROM u, pr_rice
ON CONFLICT (user_id, product_id) DO NOTHING;
INSERT INTO product_ratings (user_id, product_id, quality_score, notes, created_at, updated_at)
SELECT u.id, pr_coffee.id, 4, 'Good aroma', '2025-01-09 10:10:00+00', '2025-01-09 10:10:00+00' FROM u, pr_coffee
ON CONFLICT (user_id, product_id) DO NOTHING;
INSERT INTO product_ratings (user_id, product_id, quality_score, notes, created_at, updated_at)
SELECT b.id, pr_pasta.id, 3, 'Average pasta', '2025-01-09 10:20:00+00', '2025-01-09 10:20:00+00' FROM b, pr_pasta
ON CONFLICT (user_id, product_id) DO NOTHING;
INSERT INTO product_ratings (user_id, product_id, quality_score, notes, created_at, updated_at)
SELECT u.id, pr_milk.id, 2, 'Short shelf-life this batch', '2025-01-09 10:30:00+00', '2025-01-09 10:30:00+00' FROM u, pr_milk
ON CONFLICT (user_id, product_id) DO NOTHING;

-- =========================
-- Sanity checks (optional): quick counts
-- =========================
-- SELECT 'users' AS table, count(*) FROM users
-- UNION ALL SELECT 'categories', count(*) FROM categories
-- UNION ALL SELECT 'stores', count(*) FROM stores
-- UNION ALL SELECT 'products', count(*) FROM products
-- UNION ALL SELECT 'movements', count(*) FROM movements
-- UNION ALL SELECT 'shopping_items', count(*) FROM shopping_items
-- UNION ALL SELECT 'alerts', count(*) FROM alerts
-- UNION ALL SELECT 'price_history', count(*) FROM price_history
-- UNION ALL SELECT 'product_ratings', count(*) FROM product_ratings;
