CREATE TABLE IF NOT EXISTS clients (
    id serial PRIMARY KEY,
    login text UNIQUE NOT NULL,
    password_hash text NOT NULL,
    token text,
    active boolean NOT NULL
);
