CREATE TABLE IF NOT EXISTS "TRACK_STOCKS" (
    "ID" serial PRIMARY KEY,
    "CLIENT_ID" integer NOT NULL,
    "FIGI" text NOT NULL,
    "LOTS" integer NOT NULL,
    "STOP_LOSS" double precision NOT NULL,
    "TAKE_PROFIT" double precision NOT NULL,
    "ACTIVE" boolean NOT NULL
);
