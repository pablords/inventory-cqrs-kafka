

-- DROP TABLE IF EXISTS idempotency_keys;
-- DROP TABLE IF EXISTS outbox_events;
-- DROP TABLE IF EXISTS order_items;
-- DROP TABLE IF EXISTS orders;
-- DROP TABLE IF EXISTS products;


-- CREATE TABLE products (
--     id UUID PRIMARY KEY,
--     name VARCHAR(255) NOT NULL,
--     quantity INT NOT NULL,
--     version INT NOT NULL,
--     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
--     updated_at TIMESTAMP NOT NULL DEFAULT NOW()
-- );

-- CREATE TABLE orders (
--     id UUID PRIMARY KEY,
--     status VARCHAR(50) NOT NULL,
--     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
--     updated_at TIMESTAMP NOT NULL DEFAULT NOW()
-- );

-- CREATE TABLE order_items (
--     id UUID PRIMARY KEY,
--     order_id UUID NOT NULL,
--     product_id UUID NOT NULL,
--     quantity INT NOT NULL,
--     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
--     updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
--     FOREIGN KEY (order_id) REFERENCES orders(id),
--     FOREIGN KEY (product_id) REFERENCES products(id)
-- );

-- CREATE TABLE idempotency_keys (
--     request_id UUID PRIMARY KEY,
--     request_hash VARCHAR(64) NOT NULL,
--     status VARCHAR(20) NOT NULL,
--     response_body JSONB,
--     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
--     updated_at TIMESTAMP NOT NULL DEFAULT NOW()
-- );

-- CREATE TABLE outbox_events (
--     id UUID PRIMARY KEY,
--     aggregate_id UUID NOT NULL,
--     aggregate_type VARCHAR(50) NOT NULL,
--     event_type VARCHAR(100) NOT NULL,
--     payload JSONB NOT NULL,
--     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
--     processed_at TIMESTAMP
-- );



DELETE FROM idempotency_keys;
DELETE FROM outbox_events;
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM products;

INSERT INTO products (id, name, quantity, version, created_at, updated_at) VALUES
    ('a1b2c3d4-e5f6-4a8b-9c0d-1e2f3a4b5c6d', 'Product A', 10, 1, NOW(), NOW()),
    ('b2c3d4e5-f6a7-4b8c-9d0e-2f3a4b5c6d7e', 'Product B', 20, 1, NOW(), NOW()),
    ('c3d4e5f6-a7b8-4c9d-0e1f-3a4b5c6d7e8f', 'Product C', 30, 1, NOW(), NOW());