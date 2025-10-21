-- HomeStock – Super Complete Test Data (for unified schema)
-- Idempotente sin depender de índices únicos específicos. PostgreSQL 14+.

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
    SELECT 'Alice', 'alice@example.com', crypt('Alice123!', gen_salt('bf', 12)), 'USER', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='alice@example.com');

    INSERT INTO users (name, email, password_hash, role, created_at, updated_at)
    SELECT 'Bob', 'bob@example.com', crypt('Bob123!', gen_salt('bf', 12)), 'USER', '2025-01-06 10:00:00+00', '2025-01-06 10:00:00+00'
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='bob@example.com');

    INSERT INTO users (name, email, password_hash, role, created_at, updated_at)
    SELECT 'Admin', 'admin@homestock.test', crypt('Adm1n$tr0ng!', gen_salt('bf', 12)), 'ADMIN', '2025-01-07 11:00:00+00', '2025-01-07 11:00:00+00'
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='admin@homestock.test');
  ELSE
    -- Fallback sin pgcrypto: crea usuarios con password_hash vacío.
    INSERT INTO users (name, email, password_hash, role, created_at, updated_at)
    SELECT 'Alice', 'alice@example.com', '', 'USER', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='alice@example.com');

    INSERT INTO users (name, email, password_hash, role, created_at, updated_at)
    SELECT 'Bob', 'bob@example.com', '', 'USER', '2025-01-06 10:00:00+00', '2025-01-06 10:00:00+00'
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='bob@example.com');

    INSERT INTO users (name, email, password_hash, role, created_at, updated_at)
    SELECT 'Admin', 'admin@homestock.test', '', 'ADMIN', '2025-01-07 11:00:00+00', '2025-01-07 11:00:00+00'
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='admin@homestock.test');
  END IF;
END $$;

-- =========================
-- Categories
-- =========================
INSERT INTO categories (name, description, created_at, updated_at)
SELECT 'Pantry', 'Non-perishable pantry items', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Pantry');

INSERT INTO categories (name, description, created_at, updated_at)
SELECT 'Beverages', 'Drinks and beverages', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Beverages');

INSERT INTO categories (name, description, created_at, updated_at)
SELECT 'Dairy', 'Milk and dairy products', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Dairy');

INSERT INTO categories (name, description, created_at, updated_at)
SELECT 'Cleaning', 'House cleaning supplies', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Cleaning');

-- =========================
-- Stores
-- =========================
INSERT INTO stores (name, location, notes, created_at, updated_at)
SELECT 'SuperMart', 'Main St 123', 'Large supermarket', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'
WHERE NOT EXISTS (SELECT 1 FROM stores WHERE name='SuperMart' AND location='Main St 123');

INSERT INTO stores (name, location, notes, created_at, updated_at)
SELECT 'Local Market', '2nd Ave 45', 'Neighborhood store', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'
WHERE NOT EXISTS (SELECT 1 FROM stores WHERE name='Local Market' AND location='2nd Ave 45');

INSERT INTO stores (name, location, notes, created_at, updated_at)
SELECT 'Online Store', 'Webshop', 'E-commerce vendor', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'
WHERE NOT EXISTS (SELECT 1 FROM stores WHERE name='Online Store' AND location='Webshop');

-- =========================
-- Products (explicit all fields)
-- =========================
INSERT INTO products (user_id, name, category_id, quantity, min_stock, expiry_date, price, purchase_location_id, brand, image_url, acquisition_date, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email = 'alice@example.com'),
  'Rice 1kg',
  (SELECT id FROM categories WHERE name = 'Pantry'),
  5, 2, NULL, 2.50,
  (SELECT id FROM stores WHERE name='SuperMart' AND location='Main St 123'),
  'Generic', 'https://example.com/img/rice1kg.png', '2025-01-02', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM products
  WHERE user_id = (SELECT id FROM users WHERE email = 'alice@example.com')
    AND name = 'Rice 1kg'
);

INSERT INTO products (user_id, name, category_id, quantity, min_stock, expiry_date, price, purchase_location_id, brand, image_url, acquisition_date, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email = 'alice@example.com'),
  'Coffee 250g',
  (SELECT id FROM categories WHERE name = 'Beverages'),
  1, 2, NULL, 4.99,
  (SELECT id FROM stores WHERE name='Local Market' AND location='2nd Ave 45'),
  'Acme', 'https://example.com/img/coffee250.png', '2025-01-03', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM products
  WHERE user_id = (SELECT id FROM users WHERE email = 'alice@example.com')
    AND name = 'Coffee 250g'
);

INSERT INTO products (user_id, name, category_id, quantity, min_stock, expiry_date, price, purchase_location_id, brand, image_url, acquisition_date, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email = 'bob@example.com'),
  'Pasta 500g',
  (SELECT id FROM categories WHERE name = 'Pantry'),
  3, 1, NULL, 1.75,
  (SELECT id FROM stores WHERE name='SuperMart' AND location='Main St 123'),
  'Italiano', 'https://example.com/img/pasta500.png', '2025-01-04', '2025-01-06 10:00:00+00', '2025-01-06 10:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM products
  WHERE user_id = (SELECT id FROM users WHERE email = 'bob@example.com')
    AND name = 'Pasta 500g'
);

INSERT INTO products (user_id, name, category_id, quantity, min_stock, expiry_date, price, purchase_location_id, brand, image_url, acquisition_date, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email = 'alice@example.com'),
  'Milk 1L',
  (SELECT id FROM categories WHERE name = 'Dairy'),
  2, 2, '2025-11-01', 1.25,
  (SELECT id FROM stores WHERE name='SuperMart' AND location='Main St 123'),
  'FarmFresh', 'https://example.com/img/milk1l.png', '2025-01-04', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM products
  WHERE user_id = (SELECT id FROM users WHERE email = 'alice@example.com')
    AND name = 'Milk 1L'
);

-- Producto Bleach 2L para Admin (mantener)
INSERT INTO products (user_id, name, category_id, quantity, min_stock, expiry_date, price, purchase_location_id, brand, image_url, acquisition_date, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email = 'admin@homestock.test'),
  'Bleach 2L',
  (SELECT id FROM categories WHERE name = 'Cleaning'),
  10, 3, NULL, 3.10,
  (SELECT id FROM stores WHERE name='Online Store' AND location='Webshop'),
  'CleanCo', 'https://example.com/img/bleach2l.png', '2025-01-02', '2025-01-07 11:00:00+00', '2025-01-07 11:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM products
  WHERE user_id = (SELECT id FROM users WHERE email = 'admin@homestock.test')
    AND name = 'Bleach 2L'
);

-- Producto Bleach 2L para Alice (para que sus movimientos no fallen)
INSERT INTO products (user_id, name, category_id, quantity, min_stock, expiry_date, price, purchase_location_id, brand, image_url, acquisition_date, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email = 'alice@example.com'),
  'Bleach 2L',
  (SELECT id FROM categories WHERE name = 'Cleaning'),
  4, 3, NULL, 3.10,
  (SELECT id FROM stores WHERE name='Online Store' AND location='Webshop'),
  'CleanCo', 'https://example.com/img/bleach2l.png', '2025-01-02', '2025-01-05 09:00:00+00', '2025-01-05 09:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM products
  WHERE user_id = (SELECT id FROM users WHERE email = 'alice@example.com')
    AND name = 'Bleach 2L'
);

-- =========================
-- Movements (PURCHASE, CONSUMPTION, ADJUSTMENT) with explicit timestamps
-- =========================
-- Purchases
INSERT INTO movements (user_id, product_id, type, quantity, unit_price, store_id, note, occurred_at, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='alice@example.com'),
  (SELECT id FROM products WHERE name='Rice 1kg' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')),
  'PURCHASE', 5, 2.40,
  (SELECT id FROM stores WHERE name='SuperMart' AND location='Main St 123'),
  'Initial stock', '2025-01-05 10:00:00+00', '2025-01-05 10:00:00+00', '2025-01-05 10:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM movements
  WHERE user_id=(SELECT id FROM users WHERE email='alice@example.com')
    AND product_id=(SELECT id FROM products WHERE name='Rice 1kg' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')))
    AND type='PURCHASE'
    AND occurred_at='2025-01-05 10:00:00+00'
);

INSERT INTO movements (user_id, product_id, type, quantity, unit_price, store_id, note, occurred_at, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='alice@example.com'),
  (SELECT id FROM products WHERE name='Coffee 250g' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')),
  'PURCHASE', 2, 4.80,
  (SELECT id FROM stores WHERE name='Local Market' AND location='2nd Ave 45'),
  'Promo pack', '2025-01-06 12:00:00+00', '2025-01-06 12:00:00+00', '2025-01-06 12:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM movements
  WHERE user_id=(SELECT id FROM users WHERE email='alice@example.com')
    AND product_id=(SELECT id FROM products WHERE name='Coffee 250g' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')))
    AND type='PURCHASE'
    AND occurred_at='2025-01-06 12:00:00+00'
);

INSERT INTO movements (user_id, product_id, type, quantity, unit_price, store_id, note, occurred_at, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='bob@example.com'),
  (SELECT id FROM products WHERE name='Pasta 500g' AND user_id=(SELECT id FROM users WHERE email='bob@example.com')),
  'PURCHASE', 3, 1.60,
  (SELECT id FROM stores WHERE name='SuperMart' AND location='Main St 123'),
  'Weekly stock', '2025-01-06 15:00:00+00', '2025-01-06 15:00:00+00', '2025-01-06 15:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM movements
  WHERE user_id=(SELECT id FROM users WHERE email='bob@example.com')
    AND product_id=(SELECT id FROM products WHERE name='Pasta 500g' AND user_id=(SELECT id FROM users WHERE email='bob@example.com')))
    AND type='PURCHASE'
    AND occurred_at='2025-01-06 15:00:00+00'
);

-- Consumptions
INSERT INTO movements (user_id, product_id, type, quantity, unit_price, store_id, note, occurred_at, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='alice@example.com'),
  (SELECT id FROM products WHERE name='Rice 1kg' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')),
  'CONSUMPTION', 1, NULL, NULL,
  'Used for lunch', '2025-01-07 13:00:00+00', '2025-01-07 13:00:00+00', '2025-01-07 13:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM movements
  WHERE user_id=(SELECT id FROM users WHERE email='alice@example.com')
    AND product_id=(SELECT id FROM products WHERE name='Rice 1kg' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')))
    AND type='CONSUMPTION'
    AND occurred_at='2025-01-07 13:00:00+00'
);

INSERT INTO movements (user_id, product_id, type, quantity, unit_price, store_id, note, occurred_at, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='alice@example.com'),
  (SELECT id FROM products WHERE name='Coffee 250g' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')),
  'CONSUMPTION', 1, NULL, NULL,
  'Morning brew', '2025-01-08 07:30:00+00', '2025-01-08 07:30:00+00', '2025-01-08 07:30:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM movements
  WHERE user_id=(SELECT id FROM users WHERE email='alice@example.com')
    AND product_id=(SELECT id FROM products WHERE name='Coffee 250g' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')))
    AND type='CONSUMPTION'
    AND occurred_at='2025-01-08 07:30:00+00'
);

-- Adjustments (e.g., inventory recount)
INSERT INTO movements (user_id, product_id, type, quantity, unit_price, store_id, note, occurred_at, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='alice@example.com'),
  (SELECT id FROM products WHERE name='Milk 1L' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')),
  'ADJUSTMENT', 1, 1.30,
  (SELECT id FROM stores WHERE name='SuperMart' AND location='Main St 123'),
  'Damaged item replaced', '2025-01-08 10:00:00+00', '2025-01-08 10:00:00+00', '2025-01-08 10:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM movements
  WHERE user_id=(SELECT id FROM users WHERE email='alice@example.com')
    AND product_id=(SELECT id FROM products WHERE name='Milk 1L' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')))
    AND type='ADJUSTMENT'
    AND occurred_at='2025-01-08 10:00:00+00'
);

INSERT INTO movements (user_id, product_id, type, quantity, unit_price, store_id, note, occurred_at, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='alice@example.com'),
  (SELECT id FROM products WHERE name='Bleach 2L' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')),
  'ADJUSTMENT', 2, 3.00,
  (SELECT id FROM stores WHERE name='Online Store' AND location='Webshop'),
  'Count correction', '2025-01-09 09:00:00+00', '2025-01-09 09:00:00+00', '2025-01-09 09:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM movements
  WHERE user_id=(SELECT id FROM users WHERE email='alice@example.com')
    AND product_id=(SELECT id FROM products WHERE name='Bleach 2L' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')))
    AND type='ADJUSTMENT'
    AND occurred_at='2025-01-09 09:00:00+00'
);

-- =========================
-- Shopping Items (MANUAL & AUTO_RULE; purchased and not purchased)
-- =========================
-- Alice manual, not purchased
INSERT INTO shopping_items (user_id, product_id, desired_quantity, is_purchased, purchased_at, source, target_store_id, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='alice@example.com'),
  (SELECT id FROM products WHERE name='Coffee 250g' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')),
  2, FALSE, NULL, 'MANUAL',
  (SELECT id FROM stores WHERE name='Local Market' AND location='2nd Ave 45'),
  '2025-01-08 08:00:00+00', '2025-01-08 08:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM shopping_items
  WHERE user_id=(SELECT id FROM users WHERE email='alice@example.com')
    AND product_id=(SELECT id FROM products WHERE name='Coffee 250g' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')))
    AND is_purchased=FALSE
);

-- Bob auto_rule, purchased already
INSERT INTO shopping_items (user_id, product_id, desired_quantity, is_purchased, purchased_at, source, target_store_id, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='bob@example.com'),
  (SELECT id FROM products WHERE name='Pasta 500g' AND user_id=(SELECT id FROM users WHERE email='bob@example.com')),
  1, TRUE, '2025-01-10 16:30:00+00', 'AUTO_RULE',
  (SELECT id FROM stores WHERE name='SuperMart' AND location='Main St 123'),
  '2025-01-10 16:00:00+00', '2025-01-10 16:30:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM shopping_items
  WHERE user_id=(SELECT id FROM users WHERE email='bob@example.com')
    AND product_id=(SELECT id FROM products WHERE name='Pasta 500g' AND user_id=(SELECT id FROM users WHERE email='bob@example.com')))
    AND is_purchased=TRUE
    AND purchased_at='2025-01-10 16:30:00+00'
);

-- =========================
-- Alerts (LOW_STOCK & EXPIRY)
-- =========================
-- Low stock on coffee
INSERT INTO alerts (user_id, product_id, type, message, trigger_at, is_active, resolved_at, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='alice@example.com'),
  (SELECT id FROM products WHERE name='Coffee 250g' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')),
  'LOW_STOCK', 'Coffee below min stock', '2025-01-08 08:05:00+00', TRUE, NULL,
  '2025-01-08 08:05:00+00', '2025-01-08 08:05:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM alerts
  WHERE user_id=(SELECT id FROM users WHERE email='alice@example.com')
    AND product_id=(SELECT id FROM products WHERE name='Coffee 250g' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')))
    AND type='LOW_STOCK'
    AND trigger_at='2025-01-08 08:05:00+00'
);

-- Expiry check (general, sin producto)
INSERT INTO alerts (user_id, product_id, type, message, trigger_at, is_active, resolved_at, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='alice@example.com'),
  NULL,
  'EXPIRY', 'Check soon-to-expire items', '2025-01-08 08:10:00+00', TRUE, NULL,
  '2025-01-08 08:10:00+00', '2025-01-08 08:10:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM alerts
  WHERE user_id=(SELECT id FROM users WHERE email='alice@example.com')
    AND product_id IS NULL
    AND type='EXPIRY'
    AND trigger_at='2025-01-08 08:10:00+00'
);

-- =========================
-- Price History (multiple entries, multiple stores)
-- =========================
INSERT INTO price_history (product_id, unit_price, store_id, recorded_at, created_at, updated_at)
SELECT
  (SELECT id FROM products WHERE name='Rice 1kg' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')),
  2.40,
  (SELECT id FROM stores WHERE name='SuperMart' AND location='Main St 123'),
  '2025-01-04 12:00:00+00', '2025-01-04 12:00:00+00', '2025-01-04 12:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM price_history
  WHERE product_id=(SELECT id FROM products WHERE name='Rice 1kg' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')))
    AND store_id=(SELECT id FROM stores WHERE name='SuperMart' AND location='Main St 123')
    AND recorded_at='2025-01-04 12:00:00+00'
);

INSERT INTO price_history (product_id, unit_price, store_id, recorded_at, created_at, updated_at)
SELECT
  (SELECT id FROM products WHERE name='Rice 1kg' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')),
  2.55,
  (SELECT id FROM stores WHERE name='SuperMart' AND location='Main St 123'),
  '2025-02-01 12:00:00+00', '2025-02-01 12:00:00+00', '2025-02-01 12:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM price_history
  WHERE product_id=(SELECT id FROM products WHERE name='Rice 1kg' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')))
    AND store_id=(SELECT id FROM stores WHERE name='SuperMart' AND location='Main St 123')
    AND recorded_at='2025-02-01 12:00:00+00'
);

INSERT INTO price_history (product_id, unit_price, store_id, recorded_at, created_at, updated_at)
SELECT
  (SELECT id FROM products WHERE name='Coffee 250g' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')),
  4.80,
  (SELECT id FROM stores WHERE name='Local Market' AND location='2nd Ave 45'),
  '2025-01-05 12:00:00+00', '2025-01-05 12:00:00+00', '2025-01-05 12:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM price_history
  WHERE product_id=(SELECT id FROM products WHERE name='Coffee 250g' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')))
    AND store_id=(SELECT id FROM stores WHERE name='Local Market' AND location='2nd Ave 45')
    AND recorded_at='2025-01-05 12:00:00+00'
);

INSERT INTO price_history (product_id, unit_price, store_id, recorded_at, created_at, updated_at)
SELECT
  (SELECT id FROM products WHERE name='Coffee 250g' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')),
  5.10,
  (SELECT id FROM stores WHERE name='Local Market' AND location='2nd Ave 45'),
  '2025-02-07 12:00:00+00', '2025-02-07 12:00:00+00', '2025-02-07 12:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM price_history
  WHERE product_id=(SELECT id FROM products WHERE name='Coffee 250g' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')))
    AND store_id=(SELECT id FROM stores WHERE name='Local Market' AND location='2nd Ave 45')
    AND recorded_at='2025-02-07 12:00:00+00'
);

-- =========================
-- Product Ratings (1..5 scale)
-- =========================
INSERT INTO product_ratings (user_id, product_id, quality_score, notes, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='alice@example.com'),
  (SELECT id FROM products WHERE name='Rice 1kg' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')),
  5, 'Excellent quality rice', '2025-01-09 10:00:00+00', '2025-01-09 10:00:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM product_ratings
  WHERE user_id=(SELECT id FROM users WHERE email='alice@example.com')
    AND product_id=(SELECT id FROM products WHERE name='Rice 1kg' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')))
);

INSERT INTO product_ratings (user_id, product_id, quality_score, notes, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='alice@example.com'),
  (SELECT id FROM products WHERE name='Coffee 250g' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')),
  4, 'Good aroma', '2025-01-09 10:10:00+00', '2025-01-09 10:10:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM product_ratings
  WHERE user_id=(SELECT id FROM users WHERE email='alice@example.com')
    AND product_id=(SELECT id FROM products WHERE name='Coffee 250g' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')))
);

INSERT INTO product_ratings (user_id, product_id, quality_score, notes, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='bob@example.com'),
  (SELECT id FROM products WHERE name='Pasta 500g' AND user_id=(SELECT id FROM users WHERE email='bob@example.com')),
  3, 'Average pasta', '2025-01-09 10:20:00+00', '2025-01-09 10:20:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM product_ratings
  WHERE user_id=(SELECT id FROM users WHERE email='bob@example.com')
    AND product_id=(SELECT id FROM products WHERE name='Pasta 500g' AND user_id=(SELECT id FROM users WHERE email='bob@example.com')))
);

INSERT INTO product_ratings (user_id, product_id, quality_score, notes, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='alice@example.com'),
  (SELECT id FROM products WHERE name='Milk 1L' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')),
  2, 'Short shelf-life this batch', '2025-01-09 10:30:00+00', '2025-01-09 10:30:00+00'
WHERE NOT EXISTS (
  SELECT 1 FROM product_ratings
  WHERE user_id=(SELECT id FROM users WHERE email='alice@example.com')
    AND product_id=(SELECT id FROM products WHERE name='Milk 1L' AND user_id=(SELECT id FROM users WHERE email='alice@example.com')))
);

-- =========================
-- Sanity checks (opcionales)
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