-- =========================================================================
--  Restricciones e índices alineados con Entities.kt
-- =========================================================================

-- Unicidad
ALTER TABLE users
  ADD CONSTRAINT uq_user_email UNIQUE (email);

ALTER TABLE categories
  ADD CONSTRAINT uk_category_name UNIQUE (name);

ALTER TABLE stores
  ADD CONSTRAINT uk_store_name UNIQUE (name);

ALTER TABLE product_ratings
  ADD CONSTRAINT uk_rating_user_product UNIQUE (user_id, product_id);

-- Shopping items: una fila activa por (user, product, is_purchased)
ALTER TABLE shopping_items
  ADD CONSTRAINT uk_shopping_user_product_active UNIQUE (user_id, product_id, is_purchased);

-- Índices en Products
CREATE INDEX IF NOT EXISTS ix_product_user ON products(user_id);
CREATE INDEX IF NOT EXISTS ix_product_category ON products(category_id);
CREATE INDEX IF NOT EXISTS ix_product_store ON products(purchase_location_id);
CREATE INDEX IF NOT EXISTS ix_product_name ON products(name);

-- Índices en Movements
CREATE INDEX IF NOT EXISTS ix_movement_user ON movements(user_id);
CREATE INDEX IF NOT EXISTS ix_movement_product ON movements(product_id);
CREATE INDEX IF NOT EXISTS ix_movement_store ON movements(store_id);
CREATE INDEX IF NOT EXISTS ix_movement_created ON movements(created_at);

-- Índices en Shopping Items
CREATE INDEX IF NOT EXISTS ix_shopping_user ON shopping_items(user_id);
CREATE INDEX IF NOT EXISTS ix_shopping_product ON shopping_items(product_id);

-- Índices en Alerts
CREATE INDEX IF NOT EXISTS ix_alert_user ON alerts(user_id);
CREATE INDEX IF NOT EXISTS ix_alert_product ON alerts(product_id);
CREATE INDEX IF NOT EXISTS ix_alert_trigger ON alerts(trigger_at);
CREATE INDEX IF NOT EXISTS ix_alert_active ON alerts(is_active);

-- Índices en Price History
CREATE INDEX IF NOT EXISTS ix_ph_product ON price_history(product_id);
CREATE INDEX IF NOT EXISTS ix_ph_store ON price_history(store_id);
CREATE INDEX IF NOT EXISTS ix_ph_recorded ON price_history(recorded_at);
