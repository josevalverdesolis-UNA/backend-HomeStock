-- V4: Ajustes de unicidades, políticas ON DELETE, checks, índice adicional y triggers updated_at
SET search_path TO public;

-- =========================
-- Unicidades faltantes
-- =========================
-- Reemplazar UNIQUE(name) en stores por UNIQUE(name, location)
DO $$
BEGIN
  -- Eliminar la constraint antigua si existe
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = 'public' AND table_name = 'stores' AND constraint_name = 'uk_store_name'
  ) THEN
    ALTER TABLE stores DROP CONSTRAINT uk_store_name;
  END IF;
  -- Eliminar el índice antiguo si existe
  IF EXISTS (
    SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE c.relkind = 'i' AND c.relname = 'ix_store_name' AND n.nspname = 'public'
  ) THEN
    DROP INDEX IF EXISTS ix_store_name;
  END IF;
END $$;

-- Crear nuevo índice único y constraint USING INDEX
CREATE UNIQUE INDEX IF NOT EXISTS ix_store_name_location ON stores(name, location);
DO $$ BEGIN
  ALTER TABLE stores ADD CONSTRAINT uk_store_name_location UNIQUE USING INDEX ix_store_name_location;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Agregar UNIQUE(user_id, name) en products
CREATE UNIQUE INDEX IF NOT EXISTS ix_product_user_name ON products(user_id, name);
DO $$ BEGIN
  ALTER TABLE products ADD CONSTRAINT uk_product_user_name UNIQUE USING INDEX ix_product_user_name;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- =========================
-- Políticas ON DELETE en FKs
-- =========================
-- Dependientes: ON DELETE CASCADE
DO $$ BEGIN
  -- movements(user_id) -> users(id)
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = 'public' AND table_name = 'movements' AND constraint_name = 'fk_movement_user'
  ) THEN
    ALTER TABLE movements DROP CONSTRAINT fk_movement_user;
  END IF;
  ALTER TABLE movements ADD CONSTRAINT fk_movement_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  -- movements(product_id) -> products(id)
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = 'public' AND table_name = 'movements' AND constraint_name = 'fk_movement_product'
  ) THEN
    ALTER TABLE movements DROP CONSTRAINT fk_movement_product;
  END IF;
  ALTER TABLE movements ADD CONSTRAINT fk_movement_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  -- shopping_items(user_id) -> users(id)
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = 'public' AND table_name = 'shopping_items' AND constraint_name = 'fk_shopping_user'
  ) THEN
    ALTER TABLE shopping_items DROP CONSTRAINT fk_shopping_user;
  END IF;
  ALTER TABLE shopping_items ADD CONSTRAINT fk_shopping_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  -- shopping_items(product_id) -> products(id)
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = 'public' AND table_name = 'shopping_items' AND constraint_name = 'fk_shopping_product'
  ) THEN
    ALTER TABLE shopping_items DROP CONSTRAINT fk_shopping_product;
  END IF;
  ALTER TABLE shopping_items ADD CONSTRAINT fk_shopping_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  -- product_ratings(user_id) -> users(id)
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = 'public' AND table_name = 'product_ratings' AND constraint_name = 'fk_rating_user'
  ) THEN
    ALTER TABLE product_ratings DROP CONSTRAINT fk_rating_user;
  END IF;
  ALTER TABLE product_ratings ADD CONSTRAINT fk_rating_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  -- product_ratings(product_id) -> products(id)
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = 'public' AND table_name = 'product_ratings' AND constraint_name = 'fk_rating_product'
  ) THEN
    ALTER TABLE product_ratings DROP CONSTRAINT fk_rating_product;
  END IF;
  ALTER TABLE product_ratings ADD CONSTRAINT fk_rating_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  -- price_history(product_id) -> products(id)
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = 'public' AND table_name = 'price_history' AND constraint_name = 'fk_ph_product'
  ) THEN
    ALTER TABLE price_history DROP CONSTRAINT fk_ph_product;
  END IF;
  ALTER TABLE price_history ADD CONSTRAINT fk_ph_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Opcionales: ON DELETE SET NULL
DO $$ BEGIN
  -- products.purchase_location_id -> stores(id)
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = 'public' AND table_name = 'products' AND constraint_name = 'fk_product_purchase_location'
  ) THEN
    ALTER TABLE products DROP CONSTRAINT fk_product_purchase_location;
  END IF;
  ALTER TABLE products ADD CONSTRAINT fk_product_purchase_location FOREIGN KEY (purchase_location_id) REFERENCES stores(id) ON DELETE SET NULL;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  -- movements.store_id -> stores(id)
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = 'public' AND table_name = 'movements' AND constraint_name = 'fk_movement_store'
  ) THEN
    ALTER TABLE movements DROP CONSTRAINT fk_movement_store;
  END IF;
  ALTER TABLE movements ADD CONSTRAINT fk_movement_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE SET NULL;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  -- alerts.product_id -> products(id)
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = 'public' AND table_name = 'alerts' AND constraint_name = 'fk_alert_product'
  ) THEN
    ALTER TABLE alerts DROP CONSTRAINT fk_alert_product;
  END IF;
  ALTER TABLE alerts ADD CONSTRAINT fk_alert_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  -- price_history.store_id -> stores(id)
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = 'public' AND table_name = 'price_history' AND constraint_name = 'fk_ph_store'
  ) THEN
    ALTER TABLE price_history DROP CONSTRAINT fk_ph_store;
  END IF;
  ALTER TABLE price_history ADD CONSTRAINT fk_ph_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE SET NULL;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- =========================
-- Checks de dominio
-- =========================
-- products
DO $$ BEGIN
  ALTER TABLE products ADD CONSTRAINT ck_products_quantity_nonneg CHECK (quantity >= 0);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  ALTER TABLE products ADD CONSTRAINT ck_products_minstock_nonneg CHECK (min_stock >= 0);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  ALTER TABLE products ADD CONSTRAINT ck_products_price_nonneg_or_null CHECK (price IS NULL OR price >= 0);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- movements
DO $$ BEGIN
  ALTER TABLE movements ADD CONSTRAINT ck_movements_quantity_pos CHECK (quantity > 0);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  ALTER TABLE movements ADD CONSTRAINT ck_movements_unitprice_nonneg_or_null CHECK (unit_price IS NULL OR unit_price >= 0);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  ALTER TABLE movements ADD CONSTRAINT ck_movements_type_enum CHECK (type IN ('PURCHASE','CONSUME','ADJUST'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- shopping_items
DO $$ BEGIN
  ALTER TABLE shopping_items ADD CONSTRAINT ck_shopping_source_enum CHECK (source IN ('MANUAL','LOW_STOCK','EXPIRY'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  ALTER TABLE shopping_items ADD CONSTRAINT ck_shopping_purchase_coherence CHECK (
    (is_purchased = FALSE AND purchased_at IS NULL) OR (is_purchased = TRUE AND purchased_at IS NOT NULL)
  );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- product_ratings
DO $$ BEGIN
  ALTER TABLE product_ratings ADD CONSTRAINT ck_ratings_quality_score_range CHECK (quality_score BETWEEN 1 AND 5);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- =========================
-- Índices extra útiles
-- =========================
CREATE INDEX IF NOT EXISTS ix_shopping_is_purchased ON shopping_items(is_purchased);

-- =========================
-- updated_at automático (función + triggers)
-- =========================
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
  NEW.updated_at := now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Crear triggers BEFORE UPDATE por tabla
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_set_updated_at_users') THEN
    CREATE TRIGGER trg_set_updated_at_users BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_set_updated_at_categories') THEN
    CREATE TRIGGER trg_set_updated_at_categories BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_set_updated_at_stores') THEN
    CREATE TRIGGER trg_set_updated_at_stores BEFORE UPDATE ON stores
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_set_updated_at_products') THEN
    CREATE TRIGGER trg_set_updated_at_products BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_set_updated_at_movements') THEN
    CREATE TRIGGER trg_set_updated_at_movements BEFORE UPDATE ON movements
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_set_updated_at_shopping_items') THEN
    CREATE TRIGGER trg_set_updated_at_shopping_items BEFORE UPDATE ON shopping_items
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_set_updated_at_alerts') THEN
    CREATE TRIGGER trg_set_updated_at_alerts BEFORE UPDATE ON alerts
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_set_updated_at_price_history') THEN
    CREATE TRIGGER trg_set_updated_at_price_history BEFORE UPDATE ON price_history
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_set_updated_at_product_ratings') THEN
    CREATE TRIGGER trg_set_updated_at_product_ratings BEFORE UPDATE ON product_ratings
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;

