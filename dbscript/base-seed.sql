CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS products(
                                      id BIGSERIAL PRIMARY KEY,
                                      name TEXT NOT NULL,
                                      price NUMERIC(10,2) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

INSERT INTO products(name, price, description, created_at, updated_at)
SELECT
    'Product '|| gs,
    round((random()*1000)::numeric,2),
    'Description for Product ' || gs || ': ' || encode(digest(random()::text,'sha1'),'hex'),
    now() - (random() * interval '365 days'),
    now() - (random() * interval '30 days')
FROM generate_series(1, 10000000) gs;

ANALYZE;

select count(*) from products;