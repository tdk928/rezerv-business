-- Салон: задължителни email и телефон (контакт за резервации).
ALTER TABLE salons ADD COLUMN email VARCHAR(255);

UPDATE salons
SET email = 'salon' || id || '@demo.rezerv.bg'
WHERE email IS NULL;

ALTER TABLE salons
    ALTER COLUMN email SET NOT NULL;

ALTER TABLE salons
    ALTER COLUMN phone SET NOT NULL;
