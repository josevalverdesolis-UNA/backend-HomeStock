-- V1: Esquema base (tablas + PKs + FKs mínimas)
-- Idempotente: CREATE TABLE IF NOT EXISTS y defaults seguros

SET search_path TO public;

-- Usuarios
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  email TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Categorías
CREATE TABLE IF NOT EXISTS categories (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Tiendas
CREATE TABLE IF NOT EXISTS stores (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  location TEXT NULL,
  notes TEXT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Productos (FK mínimas: user_id, category_id)
CREATE TABLE IF NOT EXISTS products (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name TEXT NOT NULL,
  category_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  min_stock INT NOT NULL,
  expiry_date DATE NULL,
  price NUMERIC(19,4) NULL,
  purchase_location_id BIGINT NULL,
  brand TEXT NULL,
  image_url TEXT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_product_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Movimientos (FK mínimas: user_id, product_id)
CREATE TABLE IF NOT EXISTS movements (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  type VARCHAR(20) NOT NULL,
  quantity INT NOT NULL,
  unit_price NUMERIC(19,4) NULL,
  store_id BIGINT NULL,
  note TEXT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_movement_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_movement_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Lista de compras (FK mínimas: user_id, product_id)
CREATE TABLE IF NOT EXISTS shopping_items (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  is_purchased BOOLEAN NOT NULL DEFAULT FALSE,
  purchased_at TIMESTAMPTZ NULL,
  source VARCHAR(20) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_shopping_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_shopping_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Alertas (FK mínima: user_id)
CREATE TABLE IF NOT EXISTS alerts (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  product_id BIGINT NULL,
  type VARCHAR(20) NOT NULL,
  message TEXT NULL,
  trigger_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  resolved_at TIMESTAMPTZ NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_alert_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Historial de precios (FK mínima: product_id)
CREATE TABLE IF NOT EXISTS price_history (
  id BIGSERIAL PRIMARY KEY,
  product_id BIGINT NOT NULL,
  unit_price NUMERIC(19,4) NOT NULL,
  store_id BIGINT NULL,
  recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_ph_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Valoraciones (FK mínimas: user_id, product_id)
CREATE TABLE IF NOT EXISTS product_ratings (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quality_score INT NOT NULL,
  notes TEXT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_rating_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_rating_product FOREIGN KEY (product_id) REFERENCES products(id)
);

