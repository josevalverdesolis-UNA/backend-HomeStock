-- === Datos de prueba consistentes con el esquema actual (BIGINT, snake_case) ===

-- USERS
INSERT INTO users (id, name, email, created_at, updated_at)
VALUES (1, 'José', 'jose@example.com', now(), now());

-- CATEGORIES (globales)
INSERT INTO categories (id, name, description, created_at, updated_at)
VALUES (10, 'Despensa', 'Categoría general de despensa', now(), now());

-- STORES (globales)
INSERT INTO stores (id, name, location, notes, created_at, updated_at)
VALUES (20, 'AM PM', 'Sabanilla', 'Sucursal Sabanilla', now(), now());

-- PRODUCTS
INSERT INTO products (
  id, user_id, category_id, name, quantity, min_stock, expiry_date, price,
  purchase_location_id, brand, image_url, created_at, updated_at
) VALUES (
  100,
  1,
  10,
  'Arroz 1kg',
  2,
  3,
  current_date + INTERVAL '60 days',
  1200.00,
  20,
  'Tío Pelón',
  NULL,
  now(), now()
);

-- MOVEMENTS (una compra y un consumo)
INSERT INTO movements (
  id, user_id, product_id, type, quantity, unit_price, store_id, note, created_at, updated_at
) VALUES
(1000, 1, 100, 'PURCHASE', 2, 1150.00, 20, 'Compra inicial', now() - INTERVAL '1 day', now() - INTERVAL '1 day'),
(1001, 1, 100, 'CONSUMPTION', -1, NULL, 20, 'Consumo cena', now(), now());

-- SHOPPING ITEM (manual)
INSERT INTO shopping_items (
  id, user_id, product_id, quantity, is_purchased, purchased_at, source, created_at, updated_at
) VALUES (
  2000, 1, 100, 1, false, NULL, 'MANUAL', now(), now()
);

-- ALERT (LOW_STOCK)
INSERT INTO alerts (
  id, user_id, product_id, type, message, trigger_at, is_active, resolved_at, created_at, updated_at
) VALUES (
  3000, 1, 100, 'LOW_STOCK', 'Stock bajo para Arroz 1kg', now(), true, NULL, now(), now()
);

-- PRICE HISTORY
INSERT INTO price_history (id, product_id, store_id, unit_price, recorded_at, created_at, updated_at)
VALUES (4000, 100, 20, 1180.00, now(), now(), now());

-- PRODUCT RATING
INSERT INTO product_ratings (id, user_id, product_id, quality_score, notes, created_at, updated_at)
VALUES (5000, 1, 100, 5, 'Excelente calidad/precio', now(), now());
