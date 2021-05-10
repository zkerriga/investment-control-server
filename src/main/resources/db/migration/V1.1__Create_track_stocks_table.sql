CREATE TABLE IF NOT EXISTS track_stocks (
    id serial PRIMARY KEY,
    client_id integer NOT NULL,
    figi text NOT NULL,
    lots integer NOT NULL,
    stop_loss double precision NOT NULL,
    take_profit double precision NOT NULL,
    active boolean NOT NULL
);
