-- Контакти на ниво фирма (обектите ще имат свои полета отделно).
ALTER TABLE companies
    ADD COLUMN email VARCHAR(254),
    ADD COLUMN phone VARCHAR(30);

UPDATE companies
SET email = 'office@example.bg',
    phone = '+359000000000'
WHERE email IS NULL;

ALTER TABLE companies
    ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN phone SET NOT NULL;
