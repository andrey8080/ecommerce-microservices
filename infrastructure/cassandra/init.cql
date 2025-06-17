CREATE KEYSPACE IF NOT EXISTS cart_service
    WITH replication = {
        'class': 'SimpleStrategy',
        'replication_factor': 1
        };

USE cart_service;

CREATE TYPE IF NOT EXISTS cart_item (
    product_id text,
    product_name text,
    price decimal,
    quantity int
);

CREATE TABLE IF NOT EXISTS carts (
    id text PRIMARY KEY,
    userid text,
    items map<text, frozen<cart_item>>,
    created_at timestamp,
    updated_at timestamp
);
