-- V2 demo seed: EIK 204815936 има невалидна контролна сума. Коригираме на валиден dev EIK.
UPDATE companies
SET eik = '100000001'
WHERE eik = '204815936';
