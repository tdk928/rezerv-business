ALTER TABLE companies
    ADD COLUMN updated_at TIMESTAMPTZ;

UPDATE companies
SET updated_at = created_at
WHERE updated_at IS NULL;

ALTER TABLE companies
    ALTER COLUMN updated_at SET NOT NULL,
    ALTER COLUMN updated_at SET DEFAULT now();
