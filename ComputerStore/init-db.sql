-- Create the "computer_store" schema if it doesn't exist.
-- Docker Postgres auto-creates the DATABASE via POSTGRES_DB env var,
-- but the SCHEMA inside that database must be created manually.
CREATE SCHEMA IF NOT EXISTS computer_store;
