-- V2: Alinear dominio con nuevas necesidades de HomeStock (robusto con users.id y stores.id → UUID)
-- Compatible con Flyway y PostgreSQL 17. Idempotente donde es posible.

-- ============================================================
-- 0) SOLTAR TODAS LAS FKs QUE REFERENCIAN users(id) y stores(id)
-- ============================================================
DO $$
DECLARE
  r RECORD;
BEGIN
  -- FKs → users(id)
  FOR r IN
    SELECT c.conname, t.relname AS table_name
    FROM pg_constraint c
    JOIN pg_class t       ON c.conrelid = t.oid
    JOIN pg_namespace n   ON t.relnamespace = n.oid
    WHERE c.contype = 'f'
      AND c.confrelid = 'public.users'::regclass
      AND n.nspname   = 'public'
  LOOP
    EXECUTE format('ALTER TABLE public.%I DROP CONSTRAINT IF EXISTS %I', r.table_name, r.conname);
  END LOOP;

  -- FKs → stores(id)
  FOR r IN
    SELECT c.conname, t.relname AS table_name
    FROM pg_constraint c
    JOIN pg_class t       ON c.conrelid = t.oid
    JOIN pg_namespace n   ON t.relnamespace = n.oid
    WHERE c.contype = 'f'
      AND c.confrelid = 'public.stores'::regclass
      AND n.nspname   = 'public'
  LOOP
    EXECUTE format('ALTER TABLE public.%I DROP CONSTRAINT IF EXISTS %I', r.table_name, r.conname);
  END LOOP;
END$$;

-- ============================================================
-- 1) Asegurar que users.id y stores.id sean UUID (cast si eran VARCHAR)
-- ============================================================
DO $$
BEGIN
  -- users.id
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema='public' AND table_name='users' AND column_name='id' AND data_type <> 'uuid'
  ) THEN
    -- Requiere que users.id tenga valores casteables a UUID
    EXECUTE 'ALTER TABLE public.users ALTER COLUMN id TYPE uuid USING id::uuid';
  END IF;

  -- stores.id
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema='public' AND table_name='stores' AND column_name='id' AND data_type <> 'uuid'
  ) THEN
    EXECUTE 'ALTER TABLE public.stores ALTER COLUMN id TYPE uuid USING id::uuid';
  END IF;
END$$;

-- ============================================================
-- 2) Alinear TODAS las columnas que referencian users.id → UUID
--    y stores.id → UUID (si aún eran varchar/text)
-- ============================================================
DO $$
BEGIN
  -- Referencias a users.id
  PERFORM 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='categories'     AND column_name='user_id';
  IF FOUND AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='categories'     AND column_name='user_id' AND data_type <> 'uuid')
  THEN EXECUTE 'ALTER TABLE public.categories     ALTER COLUMN user_id TYPE uuid USING user_id::uuid'; END IF;

  PERFORM 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='stores'         AND column_name='user_id';
  IF FOUND AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='stores'         AND column_name='user_id' AND data_type <> 'uuid')
  THEN EXECUTE 'ALTER TABLE public.stores         ALTER COLUMN user_id TYPE uuid USING user_id::uuid'; END IF;

  PERFORM 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='products'       AND column_name='user_id';
  IF FOUND AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='products'       AND column_name='user_id' AND data_type <> 'uuid')
  THEN EXECUTE 'ALTER TABLE public.products       ALTER COLUMN user_id TYPE uuid USING user_id::uuid'; END IF;

  PERFORM 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='alerts'         AND column_name='user_id';
  IF FOUND AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='alerts'         AND column_name='user_id' AND data_type <> 'uuid')
  THEN EXECUTE 'ALTER TABLE public.alerts         ALTER COLUMN user_id TYPE uuid USING user_id::uuid'; END IF;

  PERFORM 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='shopping_items' AND column_name='user_id';
  IF FOUND AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='shopping_items' AND column_name='user_id' AND data_type <> 'uuid')
  THEN EXECUTE 'ALTER TABLE public.shopping_items ALTER COLUMN user_id TYPE uuid USING user_id::uuid'; END IF;

  PERFORM 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='product_ratings' AND column_name='user_id';
  IF FOUND AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='product_ratings' AND column_name='user_id' AND data_type <> 'uuid')
  THEN EXECUTE 'ALTER TABLE public.product_ratings ALTER COLUMN user_id TYPE uuid USING user_id::uuid'; END IF;

  -- Referencias a stores.id
  -- movements.store_id
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='movements' AND column_name='store_id') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='movements' AND column_name='store_id' AND data_type <> 'uuid')
    THEN EXECUTE 'ALTER TABLE public.movements ALTER COLUMN store_id TYPE uuid USING store_id::uuid'; END IF;
  ELSE
    EXECUTE 'ALTER TABLE public.movements ADD COLUMN store_id uuid NULL';
  END IF;

  -- price_history.store_id
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='price_history' AND column_name='store_id') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='price_history' AND column_name='store_id' AND data_type <> 'uuid')
    THEN EXECUTE 'ALTER TABLE public.price_history ALTER COLUMN store_id TYPE uuid USING store_id::uuid'; END IF;
  ELSE
    EXECUTE 'ALTER TABLE public.price_history ADD COLUMN store_id uuid NULL';
  END IF;

  -- shopping_items.target_store_id
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='shopping_items' AND column_name='target_store_id') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='shopping_items' AND column_name='target_store_id' AND data_type <> 'uuid')
    THEN EXECUTE 'ALTER TABLE public.shopping_items ALTER COLUMN target_store_id TYPE uuid USING target_store_id::uuid'; END IF;
  ELSE
    EXECUTE 'ALTER TABLE public.shopping_items ADD COLUMN target_store_id uuid NULL';
  END IF;
END$$;

-- ============================================================
-- 3) (Re)crear columnas adicionales de V2 (si faltan)
-- ============================================================
-- shopping_items.purchased_at
ALTER TABLE IF EXISTS public.shopping_items
  ADD COLUMN IF NOT EXISTS purchased_at TIMESTAMPTZ NULL;

-- alerts: type, trigger_at, is_active, resolved_at, message
ALTER TABLE IF EXISTS public.alerts
  ADD COLUMN IF NOT EXISTS type        VARCHAR(40)  DEFAULT 'GENERIC',
  ADD COLUMN IF NOT EXISTS trigger_at  TIMESTAMPTZ  NULL,
  ADD COLUMN IF NOT EXISTS is_active   BOOLEAN      DEFAULT TRUE,
  ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMPTZ  NULL,
  ADD COLUMN IF NOT EXISTS message     TEXT;

-- products.version (locking optimista)
ALTER TABLE IF EXISTS public.products
  ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- product_ratings.user_id (por si no existía)
ALTER TABLE IF EXISTS public.product_ratings
  ADD COLUMN IF NOT EXISTS user_id UUID NULL;

-- ============================================================
-- 4) Índices necesarios (idempotentes)
-- ============================================================
CREATE INDEX IF NOT EXISTS ix_categories_user                  ON public.categories(user_id);
CREATE INDEX IF NOT EXISTS ix_stores_user                      ON public.stores(user_id);
CREATE INDEX IF NOT EXISTS ix_products_user                    ON public.products(user_id);
CREATE INDEX IF NOT EXISTS ix_alerts_user                      ON public.alerts(user_id);
CREATE INDEX IF NOT EXISTS ix_alerts_active                    ON public.alerts(is_active);
CREATE INDEX IF NOT EXISTS ix_alerts_trigger_at                ON public.alerts(trigger_at);
CREATE INDEX IF NOT EXISTS ix_movements_store                  ON public.movements(store_id);
CREATE INDEX IF NOT EXISTS ix_shopping_items_target_store      ON public.shopping_items(target_store_id);
CREATE INDEX IF NOT EXISTS ix_price_history_product_store_time ON public.price_history(product_id, store_id, registered_at);

-- ============================================================
-- 5) Re-crear FKs con nombres estables (users y stores)
-- ============================================================
DO $$
BEGIN
  -- categories.user_id → users(id)
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='categories' AND column_name='user_id') THEN
    EXECUTE 'ALTER TABLE public.categories DROP CONSTRAINT IF EXISTS fk_categories_user';
    EXECUTE 'ALTER TABLE public.categories ADD CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES public.users(id)';
  END IF;

  -- stores.user_id → users(id)
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='stores' AND column_name='user_id') THEN
    EXECUTE 'ALTER TABLE public.stores DROP CONSTRAINT IF EXISTS fk_stores_user';
    EXECUTE 'ALTER TABLE public.stores ADD CONSTRAINT fk_stores_user FOREIGN KEY (user_id) REFERENCES public.users(id)';
  END IF;

  -- products.user_id → users(id)
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='products' AND column_name='user_id') THEN
    EXECUTE 'ALTER TABLE public.products DROP CONSTRAINT IF EXISTS fk_products_user';
    EXECUTE 'ALTER TABLE public.products ADD CONSTRAINT fk_products_user FOREIGN KEY (user_id) REFERENCES public.users(id)';
  END IF;

  -- alerts.user_id → users(id)
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='alerts' AND column_name='user_id') THEN
    EXECUTE 'ALTER TABLE public.alerts DROP CONSTRAINT IF EXISTS fk_alerts_user';
    EXECUTE 'ALTER TABLE public.alerts ADD CONSTRAINT fk_alerts_user FOREIGN KEY (user_id) REFERENCES public.users(id)';
  END IF;

  -- shopping_items.user_id → users(id)
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='shopping_items' AND column_name='user_id') THEN
    EXECUTE 'ALTER TABLE public.shopping_items DROP CONSTRAINT IF EXISTS fk_shopping_items_user';
    EXECUTE 'ALTER TABLE public.shopping_items ADD CONSTRAINT fk_shopping_items_user FOREIGN KEY (user_id) REFERENCES public.users(id)';
  END IF;

  -- product_ratings.user_id → users(id) + unique (user_id, product_id)
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='product_ratings' AND column_name='user_id') THEN
    EXECUTE 'ALTER TABLE public.product_ratings DROP CONSTRAINT IF EXISTS fk_product_ratings_user';
    EXECUTE 'ALTER TABLE public.product_ratings ADD CONSTRAINT fk_product_ratings_user FOREIGN KEY (user_id) REFERENCES public.users(id)';

    IF NOT EXISTS (
      SELECT 1 FROM pg_indexes WHERE schemaname='public' AND indexname='uq_rating_user_product'
    ) THEN
      EXECUTE 'CREATE UNIQUE INDEX uq_rating_user_product ON public.product_ratings(user_id, product_id)';
    END IF;
  END IF;

  -- movements.store_id → stores(id)
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='movements' AND column_name='store_id') THEN
    EXECUTE 'ALTER TABLE public.movements DROP CONSTRAINT IF EXISTS fk_movements_store';
    EXECUTE 'ALTER TABLE public.movements ADD CONSTRAINT fk_movements_store FOREIGN KEY (store_id) REFERENCES public.stores(id)';
  END IF;

  -- shopping_items.target_store_id → stores(id)
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='shopping_items' AND column_name='target_store_id') THEN
    EXECUTE 'ALTER TABLE public.shopping_items DROP CONSTRAINT IF EXISTS fk_shopping_items_target_store';
    EXECUTE 'ALTER TABLE public.shopping_items ADD CONSTRAINT fk_shopping_items_target_store FOREIGN KEY (target_store_id) REFERENCES public.stores(id)';
  END IF;

  -- price_history.store_id → stores(id)
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='price_history' AND column_name='store_id') THEN
    EXECUTE 'ALTER TABLE public.price_history DROP CONSTRAINT IF EXISTS fk_price_history_store';
    EXECUTE 'ALTER TABLE public.price_history ADD CONSTRAINT fk_price_history_store FOREIGN KEY (store_id) REFERENCES public.stores(id)';
  END IF;
END$$;

-- ============================================================
-- 6) (Opcional) Normalizar a TIMESTAMPTZ
-- ============================================================
-- ALTER TABLE public.movements      ALTER COLUMN occurred_at TYPE TIMESTAMPTZ USING occurred_at::timestamptz;
-- ALTER TABLE public.shopping_items ALTER COLUMN created_at  TYPE TIMESTAMPTZ USING created_at::timestamptz;

-- FIN V2
