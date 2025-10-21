-- HomeStock – Unified SQL (V2 consolidated V1..V6)
-- Idempotent sections use IF EXISTS and guard rails
-- Target: PostgreSQL 14+

-- Nota: no forzamos search_path aquí; Flyway ya fija el esquema actual (current_schema)

-- =========================
-- Tables
-- =========================

-- Users (includes auth fields)
CREATE TABLE IF NOT EXISTS users (
  id            BIGSERIAL PRIMARY KEY,
  name          TEXT        NOT NULL,
  email         TEXT        NOT NULL,
  password_hash TEXT        NOT NULL DEFAULT '',
  role          TEXT        NOT NULL DEFAULT 'USER',
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Categories
CREATE TABLE IF NOT EXISTS categories (
  id          BIGSERIAL PRIMARY KEY,
  name        TEXT        NOT NULL,
  description TEXT        NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Stores
CREATE TABLE IF NOT EXISTS stores (
  id         BIGSERIAL PRIMARY KEY,
  name       TEXT        NOT NULL,
  location   TEXT        NULL,
  notes      TEXT        NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Products
CREATE TABLE IF NOT EXISTS products (
  id                    BIGSERIAL PRIMARY KEY,
  user_id               BIGINT      NOT NULL,
  name                  TEXT        NOT NULL,
  category_id           BIGINT      NOT NULL,
  quantity              INT         NOT NULL,
  min_stock             INT         NOT NULL,
  expiry_date           DATE        NULL,
  price                 NUMERIC(19,4) NULL,
  purchase_location_id  BIGINT      NULL,
  brand                 TEXT        NULL,
  image_url             TEXT        NULL,
  acquisition_date      DATE        NULL,
  created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_product_user    FOREIGN KEY (user_id)    REFERENCES users(id),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Movements (stock movements)
CREATE TABLE IF NOT EXISTS movements (
  id          BIGSERIAL PRIMARY KEY,
  user_id     BIGINT      NOT NULL,
  product_id  BIGINT      NOT NULL,
  type        VARCHAR(20) NOT NULL,         -- PURCHASE | CONSUMPTION | ADJUSTMENT
  quantity    INT         NOT NULL,
  unit_price  NUMERIC(19,4) NULL,
  store_id    BIGINT      NULL,
  note        TEXT        NULL,
  occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_movement_user    FOREIGN KEY (user_id)    REFERENCES users(id),
  CONSTRAINT fk_movement_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Shopping list items
CREATE TABLE IF NOT EXISTS shopping_items (
  id               BIGSERIAL PRIMARY KEY,
  user_id          BIGINT      NOT NULL,
  product_id       BIGINT      NOT NULL,
  desired_quantity INT         NOT NULL DEFAULT 1,
  is_purchased     BOOLEAN     NOT NULL DEFAULT FALSE,
  purchased_at     TIMESTAMPTZ NULL,
  source           VARCHAR(20) NOT NULL,  -- AUTO_RULE | MANUAL
  target_store_id  BIGINT      NULL,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_shopping_user    FOREIGN KEY (user_id)    REFERENCES users(id),
  CONSTRAINT fk_shopping_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Alerts
CREATE TABLE IF NOT EXISTS alerts (
  id          BIGSERIAL PRIMARY KEY,
  user_id     BIGINT      NOT NULL,
  product_id  BIGINT      NULL,
  type        VARCHAR(20) NOT NULL,       -- EXPIRY | LOW_STOCK
  message     TEXT        NULL,
  trigger_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
  resolved_at TIMESTAMPTZ NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_alert_user    FOREIGN KEY (user_id)    REFERENCES users(id)
);

-- Price history (historical, product is RESTRICT on delete)
CREATE TABLE IF NOT EXISTS price_history (
  id          BIGSERIAL PRIMARY KEY,
  product_id  BIGINT       NOT NULL,
  unit_price  NUMERIC(19,4) NOT NULL,
  store_id    BIGINT       NULL,
  recorded_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT fk_ph_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_ph_store   FOREIGN KEY (store_id)   REFERENCES stores(id)
);

-- Product ratings
CREATE TABLE IF NOT EXISTS product_ratings (
  id            BIGSERIAL PRIMARY KEY,
  user_id       BIGINT NOT NULL,
  product_id    BIGINT NOT NULL,
  quality_score INT    NOT NULL,
  notes         TEXT   NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_rating_user    FOREIGN KEY (user_id)    REFERENCES users(id),
  CONSTRAINT fk_rating_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- =========================
-- Uniques
-- =========================
CREATE UNIQUE INDEX IF NOT EXISTS ix_users_email ON users(email);
CREATE UNIQUE INDEX IF NOT EXISTS ix_category_name ON categories(name);
DO $$ BEGIN
  ALTER TABLE categories ADD CONSTRAINT uk_category_name UNIQUE USING INDEX ix_category_name;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Stores unique by (name, location) (cleanup legacy unique if any)
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = current_schema() AND table_name = 'stores' AND constraint_name = 'uk_store_name'
  ) THEN
    ALTER TABLE stores DROP CONSTRAINT uk_store_name;
  END IF;
  IF EXISTS (
    SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE c.relkind = 'i' AND c.relname = 'ix_store_name' AND n.nspname = current_schema()
  ) THEN
    DROP INDEX IF EXISTS ix_store_name;
  END IF;
END $$;
CREATE UNIQUE INDEX IF NOT EXISTS ix_store_name_location ON stores(name, location);
DO $$ BEGIN
  ALTER TABLE stores ADD CONSTRAINT uk_store_name_location UNIQUE USING INDEX ix_store_name_location;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
-- Asegura unicidad también cuando location IS NULL (PostgreSQL permite múltiples NULL en UNIQUE normal)
CREATE UNIQUE INDEX IF NOT EXISTS ix_store_name_location_null ON stores(name) WHERE location IS NULL;

-- Product uniqueness per user
CREATE UNIQUE INDEX IF NOT EXISTS ix_product_user_name ON products(user_id, name);
DO $$ BEGIN
  ALTER TABLE products ADD CONSTRAINT uk_product_user_name UNIQUE USING INDEX ix_product_user_name;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- One rating per (user, product)
DO $$ BEGIN
  ALTER TABLE product_ratings ADD CONSTRAINT uk_rating_user_product UNIQUE (user_id, product_id);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Only one active shopping row per (user, product, is_purchased)
DO $$ BEGIN
  ALTER TABLE shopping_items ADD CONSTRAINT uk_shopping_user_product_active UNIQUE (user_id, product_id, is_purchased);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- =========================
-- Foreign key ON DELETE policies (normalize)
-- =========================

-- CASCADE dependents
DO $$ BEGIN
  ALTER TABLE movements DROP CONSTRAINT IF EXISTS fk_movement_user;
  ALTER TABLE movements ADD  CONSTRAINT fk_movement_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
END $$;
DO $$ BEGIN
  ALTER TABLE movements DROP CONSTRAINT IF EXISTS fk_movement_product;
  ALTER TABLE movements ADD  CONSTRAINT fk_movement_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;
END $$;
DO $$ BEGIN
  ALTER TABLE shopping_items DROP CONSTRAINT IF EXISTS fk_shopping_user;
  ALTER TABLE shopping_items ADD CONSTRAINT fk_shopping_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
END $$;
DO $$ BEGIN
  ALTER TABLE shopping_items DROP CONSTRAINT IF EXISTS fk_shopping_product;
  ALTER TABLE shopping_items ADD CONSTRAINT fk_shopping_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;
END $$;
DO $$ BEGIN
  ALTER TABLE product_ratings DROP CONSTRAINT IF EXISTS fk_rating_user;
  ALTER TABLE product_ratings ADD CONSTRAINT fk_rating_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
END $$;
DO $$ BEGIN
  ALTER TABLE product_ratings DROP CONSTRAINT IF EXISTS fk_rating_product;
  ALTER TABLE product_ratings ADD CONSTRAINT fk_rating_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;
END $$;

-- SET NULL optionals
DO $$ BEGIN
  ALTER TABLE products DROP CONSTRAINT IF EXISTS fk_product_purchase_location;
  ALTER TABLE products ADD  CONSTRAINT fk_product_purchase_location FOREIGN KEY (purchase_location_id) REFERENCES stores(id) ON DELETE SET NULL;
END $$;
DO $$ BEGIN
  ALTER TABLE movements DROP CONSTRAINT IF EXISTS fk_movement_store;
  ALTER TABLE movements ADD  CONSTRAINT fk_movement_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE SET NULL;
END $$;
DO $$ BEGIN
  ALTER TABLE alerts DROP CONSTRAINT IF EXISTS fk_alert_product;
  ALTER TABLE alerts ADD  CONSTRAINT fk_alert_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL;
END $$;
DO $$ BEGIN
  ALTER TABLE price_history DROP CONSTRAINT IF EXISTS fk_ph_store;
  ALTER TABLE price_history ADD CONSTRAINT fk_ph_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE SET NULL;
END $$;
DO $$ BEGIN
  -- Add missing FK for shopping_items.target_store_id
  ALTER TABLE shopping_items DROP CONSTRAINT IF EXISTS fk_shopping_target_store;
  ALTER TABLE shopping_items ADD  CONSTRAINT fk_shopping_target_store FOREIGN KEY (target_store_id) REFERENCES stores(id) ON DELETE SET NULL;
END $$;

-- Historical: RESTRICT product delete if there is price history
DO $$ BEGIN
  ALTER TABLE price_history DROP CONSTRAINT IF EXISTS fk_ph_product;
  ALTER TABLE price_history ADD CONSTRAINT fk_ph_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT;
END $$;

-- =========================
-- Domain checks
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
-- Final enum set
DO $$ BEGIN
  ALTER TABLE movements DROP CONSTRAINT IF EXISTS ck_movements_type_enum;
  ALTER TABLE movements ADD  CONSTRAINT ck_movements_type_enum CHECK (type IN ('PURCHASE','CONSUMPTION','ADJUSTMENT'));
END $$;

-- shopping_items
DO $$ BEGIN
  ALTER TABLE shopping_items ADD CONSTRAINT ck_shopping_source_enum CHECK (source IN ('AUTO_RULE','MANUAL'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  ALTER TABLE shopping_items ADD CONSTRAINT ck_shopping_purchase_coherence CHECK (
    (is_purchased = FALSE AND purchased_at IS NULL) OR (is_purchased = TRUE AND purchased_at IS NOT NULL)
  );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  ALTER TABLE shopping_items ADD CONSTRAINT ck_shopping_desired_quantity_pos CHECK (desired_quantity >= 1);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- product_ratings
DO $$ BEGIN
  ALTER TABLE product_ratings ADD CONSTRAINT ck_ratings_quality_score_range CHECK (quality_score BETWEEN 1 AND 5);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- alerts
DO $$ BEGIN
  ALTER TABLE alerts ADD CONSTRAINT ck_alerts_type_enum CHECK (type IN ('EXPIRY','LOW_STOCK'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- =========================
-- Indexes
-- =========================
-- Products
CREATE INDEX IF NOT EXISTS ix_product_user       ON products(user_id);
CREATE INDEX IF NOT EXISTS ix_product_category   ON products(category_id);
CREATE INDEX IF NOT EXISTS ix_product_store      ON products(purchase_location_id);
CREATE INDEX IF NOT EXISTS ix_product_name       ON products(name);

-- Movements
CREATE INDEX IF NOT EXISTS ix_movement_user      ON movements(user_id);
CREATE INDEX IF NOT EXISTS ix_movement_product   ON movements(product_id);
CREATE INDEX IF NOT EXISTS ix_movement_store     ON movements(store_id);
CREATE INDEX IF NOT EXISTS ix_movement_created   ON movements(created_at);
CREATE INDEX IF NOT EXISTS ix_movement_occurred  ON movements(occurred_at);

-- Shopping Items
CREATE INDEX IF NOT EXISTS ix_shopping_user         ON shopping_items(user_id);
CREATE INDEX IF NOT EXISTS ix_shopping_product      ON shopping_items(product_id);
CREATE INDEX IF NOT EXISTS ix_shopping_is_purchased ON shopping_items(is_purchased);
CREATE INDEX IF NOT EXISTS ix_shopping_target_store ON shopping_items(target_store_id);

-- Alerts
CREATE INDEX IF NOT EXISTS ix_alert_user    ON alerts(user_id);
CREATE INDEX IF NOT EXISTS ix_alert_product ON alerts(product_id);
CREATE INDEX IF NOT EXISTS ix_alert_trigger ON alerts(trigger_at);
CREATE INDEX IF NOT EXISTS ix_alert_active  ON alerts(is_active);

-- Price History
CREATE INDEX IF NOT EXISTS ix_ph_product  ON price_history(product_id);
CREATE INDEX IF NOT EXISTS ix_ph_store    ON price_history(store_id);
CREATE INDEX IF NOT EXISTS ix_ph_recorded ON price_history(recorded_at);

-- Ratings
CREATE INDEX IF NOT EXISTS ix_rating_user    ON product_ratings(user_id);
CREATE INDEX IF NOT EXISTS ix_rating_product ON product_ratings(product_id);

-- =========================
-- Triggers: auto-updated updated_at
-- =========================
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
  NEW.updated_at := now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

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
