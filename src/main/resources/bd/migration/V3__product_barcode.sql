-- V3: Add optional barcode to products and helpful indexes
-- Safe/idempotent guards for PostgreSQL

ALTER TABLE products
  ADD COLUMN IF NOT EXISTS barcode TEXT NULL;

-- Index to speed search by barcode alone
CREATE INDEX IF NOT EXISTS ix_product_barcode ON products(barcode);
-- Composite index for typical lookup by (user_id, barcode)
CREATE INDEX IF NOT EXISTS ix_product_user_barcode ON products(user_id, barcode);

