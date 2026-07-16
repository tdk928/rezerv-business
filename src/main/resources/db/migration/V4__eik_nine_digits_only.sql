-- ЕИК в onboarding е само 9-цифрен (без 13-цифрен клон).
ALTER TABLE companies DROP CONSTRAINT companies_eik_check;
ALTER TABLE companies
    ADD CONSTRAINT companies_eik_check CHECK (eik ~ '^[0-9]{9}$');
ALTER TABLE companies
    ALTER COLUMN eik TYPE VARCHAR(9);
