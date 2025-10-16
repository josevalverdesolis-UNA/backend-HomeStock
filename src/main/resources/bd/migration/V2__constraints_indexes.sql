-- V2: Restricciones únicas, llaves foráneas adicionales e índices
SET search_path TO public;

-- Unicidades
CREATE UNIQUE INDEX IF NOT EXISTS ix_users_email ON users(email);
CREATE UNIQUE INDEX IF NOT EXISTS ix_category_name ON categories(name);
DO $$ BEGIN
  ALTER TABLE categories ADD CONSTRAINT uk_category_name UNIQUE USING INDEX ix_category_name;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

CREATE UNIQUE INDEX IF NOT EXISTS ix_store_name ON stores(name);
DO $$ BEGIN
  ALTER TABLE stores ADD CONSTRAINT uk_store_name UNIQUE USING INDEX ix_store_name;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  ALTER TABLE product_ratings ADD CONSTRAINT uk_rating_user_product UNIQUE (user_id, product_id);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  ALTER TABLE shopping_items ADD CONSTRAINT uk_shopping_user_product_active UNIQUE (user_id, product_id, is_purchased);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- FKs adicionales
DO $$ BEGIN
  ALTER TABLE products ADD CONSTRAINT fk_product_purchase_location FOREIGN KEY (purchase_location_id) REFERENCES stores(id);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  ALTER TABLE movements ADD CONSTRAINT fk_movement_store FOREIGN KEY (store_id) REFERENCES stores(id);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  ALTER TABLE alerts ADD CONSTRAINT fk_alert_product FOREIGN KEY (product_id) REFERENCES products(id);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  ALTER TABLE price_history ADD CONSTRAINT fk_ph_store FOREIGN KEY (store_id) REFERENCES stores(id);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Índices de consulta (según Entities.kt)
-- Products
CREATE INDEX IF NOT EXISTS ix_product_user ON products(user_id);
CREATE INDEX IF NOT EXISTS ix_product_category ON products(category_id);
CREATE INDEX IF NOT EXISTS ix_product_store ON products(purchase_location_id);
CREATE INDEX IF NOT EXISTS ix_product_name ON products(name);

-- Movements
CREATE INDEX IF NOT EXISTS ix_movement_user ON movements(user_id);
CREATE INDEX IF NOT EXISTS ix_movement_product ON movements(product_id);
CREATE INDEX IF NOT EXISTS ix_movement_store ON movements(store_id);
CREATE INDEX IF NOT EXISTS ix_movement_created ON movements(created_at);

-- Shopping Items
CREATE INDEX IF NOT EXISTS ix_shopping_user ON shopping_items(user_id);
CREATE INDEX IF NOT EXISTS ix_shopping_product ON shopping_items(product_id);

-- Alerts
CREATE INDEX IF NOT EXISTS ix_alert_user ON alerts(user_id);
CREATE INDEX IF NOT EXISTS ix_alert_product ON alerts(product_id);
CREATE INDEX IF NOT EXISTS ix_alert_trigger ON alerts(trigger_at);
CREATE INDEX IF NOT EXISTS ix_alert_active ON alerts(is_active);

-- Price History
CREATE INDEX IF NOT EXISTS ix_ph_product ON price_history(product_id);
CREATE INDEX IF NOT EXISTS ix_ph_store ON price_history(store_id);
CREATE INDEX IF NOT EXISTS ix_ph_recorded ON price_history(recorded_at);

-- Product Ratings
CREATE INDEX IF NOT EXISTS ix_rating_user ON product_ratings(user_id);
CREATE INDEX IF NOT EXISTS ix_rating_product ON product_ratings(product_id);

