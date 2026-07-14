-- Номенклатури: градове (областните центрове) и категории услуги.
-- Управляват се от PLATFORM_ADMIN; бизнесите само избират от тях.

CREATE TABLE cities (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE service_categories (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    icon VARCHAR(50)  NOT NULL
);

-- 27-те областни града (28 области; София е център и на София-град, и на Софийска област)
INSERT INTO cities (name, slug) VALUES
    ('Благоевград',   'blagoevgrad'),
    ('Бургас',        'burgas'),
    ('Варна',         'varna'),
    ('Велико Търново','veliko-tarnovo'),
    ('Видин',         'vidin'),
    ('Враца',         'vratsa'),
    ('Габрово',       'gabrovo'),
    ('Добрич',        'dobrich'),
    ('Кърджали',      'kardzhali'),
    ('Кюстендил',     'kyustendil'),
    ('Ловеч',         'lovech'),
    ('Монтана',       'montana'),
    ('Пазарджик',     'pazardzhik'),
    ('Перник',        'pernik'),
    ('Плевен',        'pleven'),
    ('Пловдив',       'plovdiv'),
    ('Разград',       'razgrad'),
    ('Русе',          'ruse'),
    ('Силистра',      'silistra'),
    ('Сливен',        'sliven'),
    ('Смолян',        'smolyan'),
    ('София',         'sofia'),
    ('Стара Загора',  'stara-zagora'),
    ('Търговище',     'targovishte'),
    ('Хасково',       'haskovo'),
    ('Шумен',         'shumen'),
    ('Ямбол',         'yambol');

-- Начални категории услуги; icon е код, който frontend-ът мапва към икона
INSERT INTO service_categories (name, slug, icon) VALUES
    ('Фризьор',            'frizyor',           'scissors'),
    ('Барбер',             'barber',            'razor'),
    ('Маникюр и педикюр',  'manikyur-pedikyur', 'nail-polish'),
    ('Масаж',              'masazh',            'massage'),
    ('Козметика',          'kozmetika',         'face'),
    ('Грим',               'grim',              'brush'),
    ('Вежди и мигли',      'vezhdi-migli',      'eye'),
    ('Депилация',          'depilatsiya',       'wax');
