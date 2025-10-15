-- === Datos de prueba consistentes con el esquema actual ===

-- USERS
INSERT INTO users (id, name, email)
VALUES ('11111111-1111-1111-1111-111111111111', 'José', 'jose@example.com');

-- CATEGORIES
INSERT INTO categories (id, user_id, name)
VALUES ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'Despensa');

-- STORES
INSERT INTO stores (id, user_id, name, location)
VALUES ('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 'AM PM', 'Sabanilla');

-- PRODUCTS
INSERT INTO products (
  id, user_id, category_id, store_id, name, brand, quantity, min_stock,
  acquisition_date, price, image_url
) VALUES (
  '44444444-4444-4444-4444-444444444444',
  '11111111-1111-1111-1111-111111111111',
  '22222222-2222-2222-2222-222222222222',
  '33333333-3333-3333-3333-333333333333',
  'Arroz 1kg', 'Tío Pelón',
  2, 3,
  current_date,
  1200.00,
  NULL
);

-- MOVEMENTS (una compra y un consumo)
INSERT INTO movements (
  id, product_id, type, quantity, unit_price, note, occurred_at
) VALUES
('55555555-5555-5555-5555-555555555555', '44444444-4444-4444-4444-444444444444',
 'PURCHASE', 2, 1150.00, 'Compra inicial', now() - INTERVAL '1 day'),
('66666666-6666-6666-6666-666666666666', '44444444-4444-4444-4444-444444444444',
 'CONSUMPTION', -1, NULL, 'Consumo cena', now());

-- SHOPPING ITEM (manual)
INSERT INTO shopping_items (
  id, product_id, quantity, is_purchased, source, created_at
) VALUES (
  '77777777-7777-7777-7777-777777777777',
  '44444444-4444-4444-4444-444444444444',
  1, false, 'MANUAL', now()
);

-- ALERT (LOW_STOCK)
INSERT INTO alerts (id, user_id, product_id, type, message, created_at, resolved)
VALUES (
  '88888888-8888-8888-8888-888888888888',
  '11111111-1111-1111-1111-111111111111',
  '44444444-4444-4444-4444-444444444444',
  'LOW_STOCK', 'Stock bajo para Arroz 1kg', now(), false
);

-- PRICE HISTORY
INSERT INTO price_history (id, product_id, price, registered_at)
VALUES (
  '99999999-9999-9999-9999-999999999999',
  '44444444-4444-4444-4444-444444444444',
  1180.00, now()
);

-- PRODUCT RATING
INSERT INTO product_ratings (id, product_id, score, comment, created_at)
VALUES (
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  '44444444-4444-4444-4444-444444444444',
  5, 'Excelente calidad/precio', now()
);
