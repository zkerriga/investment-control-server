CREATE TABLE IF NOT EXISTS "NOTIFICATIONS" (
    "ID" serial PRIMARY KEY,
    "CLIENT_ID" integer NOT NULL,
    "TRACK_STOCK_ID" integer NOT NULL,
    "SOLD" boolean NOT NULL,
    "SENT" boolean NOT NULL
);
