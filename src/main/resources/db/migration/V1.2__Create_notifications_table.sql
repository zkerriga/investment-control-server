CREATE TABLE IF NOT EXISTS notifications (
    id serial PRIMARY KEY,
    client_id integer NOT NULL,
    track_stock_id integer NOT NULL,
    sold boolean NOT NULL,
    sent boolean NOT NULL
);
