CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    quantity INTEGER,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);