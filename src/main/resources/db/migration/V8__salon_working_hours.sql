-- Работно време на обект (staff_id NULL = ниво салон). ISO day: 1=пн … 7=нд.
-- Wipe на фирми/обекти — working hours са задължителни; старите записи без часове са невалидни.

CREATE TABLE working_hours (
    id           BIGSERIAL PRIMARY KEY,
    salon_id     BIGINT      NOT NULL REFERENCES salons (id) ON DELETE CASCADE,
    staff_id     BIGINT,
    day_of_week  SMALLINT    NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time   TIME        NOT NULL,
    end_time     TIME        NOT NULL,
    CONSTRAINT chk_working_hours_range CHECK (start_time < end_time)
);

CREATE UNIQUE INDEX uq_working_hours_salon_day
    ON working_hours (salon_id, day_of_week)
    WHERE staff_id IS NULL;

CREATE INDEX idx_working_hours_salon ON working_hours (salon_id);

-- Изчистване на текущи фирми/обекти/услуги/снимки (номенклатурите cities/categories остават).
TRUNCATE TABLE salon_services, salon_photos, salons, companies RESTART IDENTITY CASCADE;

-- Демо seed отново + работно време (пн–пет 09:00–18:00, съб 09:00–14:00).
INSERT INTO companies (eik, name, legal_name, owner_user_id, status, email, phone) VALUES
    ('204815936', 'Гламур Груп',  '„Гламур Груп" ЕООД',  1, 'APPROVED', 'glamour@example.bg', '+359888111223'),
    ('131529327', 'Барбер Брос',  '„Барбер Брос" ООД',   1, 'APPROVED', 'barber@example.bg',  '+359888333445');

INSERT INTO salons (company_id, name, description, city_id, address, lat, lng, email, phone, rating_avg, rating_count) VALUES
    (1, 'Studio Glamour',   'Модерно студио за коса и красота в сърцето на София.',
        (SELECT id FROM cities WHERE slug = 'sofia'),   'бул. Витоша 45',        42.6934, 23.3202, 'studio@glamour.bg', '+359 88 111 2233', 4.80, 124),
    (1, 'Nails & Beauty',   'Маникюр, педикюр и грижа за кожата с внимание към детайла.',
        (SELECT id FROM cities WHERE slug = 'sofia'),   'ул. Раковски 128',      42.6920, 23.3260, 'nails@glamour.bg',  '+359 88 222 3344', 4.60,  87),
    (2, 'Barber Bros',      'Класически барбершоп — прецизни фейдове и грижа за брадата.',
        (SELECT id FROM cities WHERE slug = 'sofia'),   'ул. Шишман 12',         42.6900, 23.3310, 'hello@barberbros.bg','+359 88 333 4455', 4.90, 211),
    (1, 'Zen Spa Пловдив',  'Спа оазис за масажи и релакс в Капана.',
        (SELECT id FROM cities WHERE slug = 'plovdiv'), 'ул. Отец Паисий 24',    42.1471, 24.7480, 'zen@glamour.bg',    '+359 88 444 5566', 4.70,  95),
    (2, 'Морски бряг',      'Фризьорски салон с гледка към морската градина.',
        (SELECT id FROM cities WHERE slug = 'varna'),   'бул. Сливница 33',      43.2050, 27.9180, 'more@barberbros.bg','+359 88 555 6677', 4.50,  63),
    (1, 'Бургас Стайл',     'Пълна гама услуги — коса, грим и вежди.',
        (SELECT id FROM cities WHERE slug = 'burgas'),  'ул. Александровска 71', 42.4940, 27.4720, 'burgas@glamour.bg', '+359 88 666 7788', 4.40,  42);

INSERT INTO salon_photos (salon_id, url, position) VALUES
    (1, 'https://picsum.photos/seed/rezerv-salon1a/800/600', 0),
    (1, 'https://picsum.photos/seed/rezerv-salon1b/800/600', 1),
    (2, 'https://picsum.photos/seed/rezerv-salon2a/800/600', 0),
    (3, 'https://picsum.photos/seed/rezerv-salon3a/800/600', 0),
    (3, 'https://picsum.photos/seed/rezerv-salon3b/800/600', 1),
    (4, 'https://picsum.photos/seed/rezerv-salon4a/800/600', 0),
    (5, 'https://picsum.photos/seed/rezerv-salon5a/800/600', 0),
    (6, 'https://picsum.photos/seed/rezerv-salon6a/800/600', 0);

INSERT INTO salon_services (salon_id, category_id, name, duration_min, price) VALUES
    (1, (SELECT id FROM service_categories WHERE slug = 'frizyor'),           'Дамско подстригване',       60,  55.00),
    (1, (SELECT id FROM service_categories WHERE slug = 'frizyor'),           'Боядисване с амоняк-free',  120, 130.00),
    (1, (SELECT id FROM service_categories WHERE slug = 'grim'),              'Официален грим',            60,  90.00),
    (2, (SELECT id FROM service_categories WHERE slug = 'manikyur-pedikyur'), 'Маникюр с гел лак',         60,  45.00),
    (2, (SELECT id FROM service_categories WHERE slug = 'manikyur-pedikyur'), 'Педикюр',                   75,  55.00),
    (2, (SELECT id FROM service_categories WHERE slug = 'kozmetika'),         'Дълбоко почистване на лице', 90,  80.00),
    (3, (SELECT id FROM service_categories WHERE slug = 'barber'),            'Мъжко подстригване + фейд', 45,  35.00),
    (3, (SELECT id FROM service_categories WHERE slug = 'barber'),            'Оформяне на брада',         30,  25.00),
    (4, (SELECT id FROM service_categories WHERE slug = 'masazh'),            'Класически масаж 60 мин',   60,  70.00),
    (4, (SELECT id FROM service_categories WHERE slug = 'masazh'),            'Дълбокотъканен масаж',      90, 105.00),
    (5, (SELECT id FROM service_categories WHERE slug = 'frizyor'),           'Мъжко подстригване',        40,  30.00),
    (5, (SELECT id FROM service_categories WHERE slug = 'frizyor'),           'Дамско подстригване',       60,  50.00),
    (6, (SELECT id FROM service_categories WHERE slug = 'vezhdi-migli'),      'Оформяне на вежди',         30,  25.00),
    (6, (SELECT id FROM service_categories WHERE slug = 'grim'),              'Дневен грим',               45,  60.00);

-- Пн–Пет 09–18, Съб 09–14 за всички демо салони
INSERT INTO working_hours (salon_id, staff_id, day_of_week, start_time, end_time)
SELECT s.id, NULL, d.day_of_week, d.start_time::time, d.end_time::time
FROM salons s
CROSS JOIN (
    VALUES
        (1, '09:00', '18:00'),
        (2, '09:00', '18:00'),
        (3, '09:00', '18:00'),
        (4, '09:00', '18:00'),
        (5, '09:00', '18:00'),
        (6, '09:00', '14:00')
) AS d(day_of_week, start_time, end_time);
