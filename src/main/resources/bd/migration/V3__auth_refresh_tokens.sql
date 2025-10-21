-- V3: Tabla de refresh tokens para autenticación JWT (access/refresh)

CREATE TABLE IF NOT EXISTS refresh_tokens (
  id          BIGSERIAL PRIMARY KEY,
  user_id     BIGINT       NOT NULL,
  token_hash  TEXT         NOT NULL,
  expires_at  TIMESTAMPTZ  NOT NULL,
  revoked_at  TIMESTAMPTZ  NULL,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Índices y unicidad del hash (para lookup rápido y evitar duplicados)
CREATE UNIQUE INDEX IF NOT EXISTS uk_rt_token_hash ON refresh_tokens(token_hash);
CREATE INDEX IF NOT EXISTS ix_rt_user    ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS ix_rt_expires ON refresh_tokens(expires_at);
CREATE INDEX IF NOT EXISTS ix_rt_revoked ON refresh_tokens(revoked_at);

