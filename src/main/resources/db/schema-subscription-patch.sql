-- Run manually in PostgreSQL if needed (Neon SQL editor / psql)
-- Safe to run multiple times.

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS creator_membership_active BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS investor_membership_active BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE payment
  ADD COLUMN IF NOT EXISTS payment_type VARCHAR(32) NOT NULL DEFAULT 'INVESTMENT';

ALTER TABLE payment
  ADD COLUMN IF NOT EXISTS receipt_number VARCHAR(64);

ALTER TABLE investments
  ADD COLUMN IF NOT EXISTS payment_id BIGINT;
