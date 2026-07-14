-- Фирми (по ЕИК), салони, снимки и услуги на салон + демо seed за разработка.

CREATE TABLE companies (
    id            BIGSERIAL PRIMARY KEY,
    eik           VARCHAR(13) NOT NULL UNIQUE CHECK (eik ~ '^[0-9]{9}$|^[0-9]{13}$'),
    name          VARCHAR(200) NOT NULL,
    legal_name    VARCHAR(200) NOT NULL,
    owner_user_id BIGINT       NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING_APPROVAL'
                  CHECK (status IN ('PENDING_APPROVAL', 'APPROVED', 'SUSPENDED')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE salons (
    id           BIGSERIAL PRIMARY KEY,
    company_id   BIGINT       NOT NULL REFERENCES companies (id),
    name         VARCHAR(200) NOT NULL,
    description  TEXT,
    city_id      BIGINT       NOT NULL REFERENCES cities (id),
    address      VARCHAR(300) NOT NULL,
    lat          DOUBLE PRECISION,
    lng          DOUBLE PRECISION,
    phone        VARCHAR(30),
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    rating_avg   NUMERIC(3, 2) NOT NULL DEFAULT 0,
    rating_count INT           NOT NULL DEFAULT 0
);

CREATE INDEX idx_salons_city ON salons (city_id);
CREATE INDEX idx_salons_company ON salons (company_id);

CREATE TABLE salon_photos (
    id       BIGSERIAL PRIMARY KEY,
    salon_id BIGINT       NOT NULL REFERENCES salons (id),
    url      VARCHAR(500) NOT NULL,
    position INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_salon_photos_salon ON salon_photos (salon_id);

CREATE TABLE salon_services (
    id           BIGSERIAL PRIMARY KEY,
    salon_id     BIGINT        NOT NULL REFERENCES salons (id),
    category_id  BIGINT        NOT NULL REFERENCES service_categories (id),
    name         VARCHAR(200)  NOT NULL,
    duration_min INT           NOT NULL CHECK (duration_min > 0),
    price        NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    active       BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_salon_services_salon ON salon_services (salon_id);
CREATE INDEX idx_salon_services_category ON salon_services (category_id);

-- ===== ДЕМО SEED (само за dev; ще се маха/замества при реалния onboarding) =====

INSERT INTO companies (eik, name, legal_name, owner_user_id, status) VALUES
    ('204815936', 'Гламур Груп',  '„Гламур Груп" ЕООД',  1, 'APPROVED'),
    ('131529327', 'Барбер Брос',  '„Барбер Брос" ООД',   1, 'APPROVED');

INSERT INTO salons (company_id, name, description, city_id, address, lat, lng, phone, rating_avg, rating_count) VALUES
    (1, 'Studio Glamour',   'Модерно студио за коса и красота в сърцето на София.',
        (SELECT id FROM cities WHERE slug = 'sofia'),   'бул. Витоша 45',        42.6934, 23.3202, '+359 88 111 2233', 4.80, 124),
    (1, 'Nails & Beauty',   'Маникюр, педикюр и грижа за кожата с внимание към детайла.',
        (SELECT id FROM cities WHERE slug = 'sofia'),   'ул. Раковски 128',      42.6920, 23.3260, '+359 88 222 3344', 4.60,  87),
    (2, 'Barber Bros',      'Класически барбершоп — прецизни фейдове и грижа за брадата.',
        (SELECT id FROM cities WHERE slug = 'sofia'),   'ул. Шишман 12',         42.6900, 23.3310, '+359 88 333 4455', 4.90, 211),
    (1, 'Zen Spa Пловдив',  'Спа оазис за масажи и релакс в Капана.',
        (SELECT id FROM cities WHERE slug = 'plovdiv'), 'ул. Отец Паисий 24',    42.1471, 24.7480, '+359 88 444 5566', 4.70,  95),
    (2, 'Морски бряг',      'Фризьорски салон с гледка към морската градина.',
        (SELECT id FROM cities WHERE slug = 'varna'),   'бул. Сливница 33',      43.2050, 27.9180, '+359 88 555 6677', 4.50,  63),
    (1, 'Бургас Стайл',     'Пълна гама услуги — коса, грим и вежди.',
        (SELECT id FROM cities WHERE slug = 'burgas'),  'ул. Александровска 71', 42.4940, 27.4720, '+359 88 666 7788', 4.40,  42);

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
