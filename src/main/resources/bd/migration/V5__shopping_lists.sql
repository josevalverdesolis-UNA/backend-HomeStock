-- V4: Shopping lists (agrupadas) y sus ítems
-- Crea tablas shopping_lists y shopping_list_items

-- shopping_lists
CREATE TABLE IF NOT EXISTS shopping_lists (
  id          BIGSERIAL PRIMARY KEY,
  user_id     BIGINT      NOT NULL,
  name        TEXT        NOT NULL,
  note        TEXT        NULL,
  status      VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT|COMPLETED|CANCELLED
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_shopl_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_shopl_user   ON shopping_lists(user_id);
CREATE INDEX IF NOT EXISTS ix_shopl_status ON shopping_lists(status);

-- shopping_list_items
CREATE TABLE IF NOT EXISTS shopping_list_items (
  id               BIGSERIAL PRIMARY KEY,
  list_id          BIGINT      NOT NULL,
  product_id       BIGINT      NOT NULL,
  desired_quantity INT         NOT NULL DEFAULT 1,
  is_checked       BOOLEAN     NOT NULL DEFAULT FALSE,
  checked_at       TIMESTAMPTZ NULL,
  target_store_id  BIGINT      NULL,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_shoplitem_list    FOREIGN KEY (list_id)    REFERENCES shopping_lists(id) ON DELETE CASCADE,
  CONSTRAINT fk_shoplitem_product FOREIGN KEY (product_id) REFERENCES products(id)       ON DELETE CASCADE,
  CONSTRAINT fk_shoplitem_store   FOREIGN KEY (target_store_id) REFERENCES stores(id)   ON DELETE SET NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS ix_shoplitem_unique ON shopping_list_items(list_id, product_id);
DO $$ BEGIN
  ALTER TABLE shopping_list_items ADD CONSTRAINT uk_shoplitem_list_product UNIQUE USING INDEX ix_shoplitem_unique;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

CREATE INDEX IF NOT EXISTS ix_shoplitem_list    ON shopping_list_items(list_id);
CREATE INDEX IF NOT EXISTS ix_shoplitem_product ON shopping_list_items(product_id);

-- Checks de dominio
DO $$ BEGIN
  ALTER TABLE shopping_lists ADD CONSTRAINT ck_shopl_status_enum CHECK (status IN ('DRAFT','COMPLETED','CANCELLED'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  ALTER TABLE shopping_list_items ADD CONSTRAINT ck_shoplitem_quantity_pos CHECK (desired_quantity >= 1);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Triggers updated_at
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_set_updated_at_shopping_lists') THEN
    CREATE TRIGGER trg_set_updated_at_shopping_lists BEFORE UPDATE ON shopping_lists
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_set_updated_at_shopping_list_items') THEN
    CREATE TRIGGER trg_set_updated_at_shopping_list_items BEFORE UPDATE ON shopping_list_items
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;

