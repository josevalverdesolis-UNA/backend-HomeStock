-- V5: Alineación de dominio (enums, columnas nuevas/renombres, FKs históricas, índices)
SET search_path TO public;

-- -------------------------
-- Products: acquisition_date
-- -------------------------
ALTER TABLE IF EXISTS products
  ADD COLUMN IF NOT EXISTS acquisition_date DATE NULL;

-- -------------------------
-- Movements: occurred_at + índice
-- -------------------------
ALTER TABLE IF EXISTS movements
  ADD COLUMN IF NOT EXISTS occurred_at TIMESTAMPTZ;
-- Asegurar NOT NULL con default para filas existentes
DO $$ BEGIN
  UPDATE movements SET occurred_at = COALESCE(occurred_at, now());
  ALTER TABLE movements ALTER COLUMN occurred_at SET NOT NULL;
EXCEPTION WHEN undefined_column THEN NULL; END $$;

CREATE INDEX IF NOT EXISTS ix_movement_occurred ON movements(occurred_at);

-- -------------------------
-- Shopping Items: rename quantity -> desired_quantity, add target_store_id + índice
-- -------------------------
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'shopping_items' AND column_name = 'quantity'
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'shopping_items' AND column_name = 'desired_quantity'
  ) THEN
    ALTER TABLE shopping_items RENAME COLUMN quantity TO desired_quantity;
  END IF;
END $$;

ALTER TABLE IF EXISTS shopping_items
  ADD COLUMN IF NOT EXISTS target_store_id BIGINT NULL;

DO $$ BEGIN
  ALTER TABLE shopping_items
    ADD CONSTRAINT fk_shopping_target_store FOREIGN KEY (target_store_id) REFERENCES stores(id) ON DELETE SET NULL;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

CREATE INDEX IF NOT EXISTS ix_shopping_target_store ON shopping_items(target_store_id);

-- Check de dominio: desired_quantity >= 1
DO $$ BEGIN
  ALTER TABLE shopping_items ADD CONSTRAINT ck_shopping_desired_quantity_pos CHECK (desired_quantity >= 1);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- -------------------------
-- Enums como CHECKs de dominio
-- -------------------------
-- Movements.type: corregir al set (PURCHASE, CONSUMPTION, ADJUSTMENT)
ALTER TABLE IF EXISTS movements DROP CONSTRAINT IF EXISTS ck_movements_type_enum;
DO $$ BEGIN
  ALTER TABLE movements ADD CONSTRAINT ck_movements_type_enum CHECK (type IN ('PURCHASE','CONSUMPTION','ADJUSTMENT'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- ShoppingItems.source: corregir al set (AUTO_RULE, MANUAL)
ALTER TABLE IF EXISTS shopping_items DROP CONSTRAINT IF EXISTS ck_shopping_source_enum;
DO $$ BEGIN
  ALTER TABLE shopping_items ADD CONSTRAINT ck_shopping_source_enum CHECK (source IN ('AUTO_RULE','MANUAL'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Alerts.type: añadir check (EXPIRY, LOW_STOCK)
DO $$ BEGIN
  ALTER TABLE alerts ADD CONSTRAINT ck_alerts_type_enum CHECK (type IN ('EXPIRY','LOW_STOCK'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- -------------------------
-- FKs históricas: RESTRICT para price_history(product_id)
-- -------------------------
DO $$ BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = 'public' AND table_name = 'price_history' AND constraint_name = 'fk_ph_product'
  ) THEN
    ALTER TABLE price_history DROP CONSTRAINT fk_ph_product;
  END IF;
  ALTER TABLE price_history ADD CONSTRAINT fk_ph_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- store_id en price_history se mantiene opcional con ON DELETE SET NULL (ya aplicado en V4)

-- -------------------------
-- Índices adicionales de apoyo (ya creados en V4 para shopping_items.is_purchased)
-- -------------------------
-- No adicionales aquí.
