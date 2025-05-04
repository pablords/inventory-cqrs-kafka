CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    quantity INTEGER,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS outbox_event (
  id UUID PRIMARY KEY,
  aggregate_type VARCHAR(255),
  aggregate_id VARCHAR(255),
  type VARCHAR(255),
  payload JSONB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX IF NOT EXISTS idx_outbox_created_at ON outbox_event(created_at);
ALTER TABLE public.outbox_event REPLICA IDENTITY FULL;