# feature/nomenclatures-cities-categories

## Цел
База данни + номенклатурите, които хранят търсачката и категорийните плочки на home page:
градове и категории услуги, с публични ендпойнти за frontend-а.

## Направено
- **DB инфраструктура**: `spring-boot-starter-data-jpa`, PostgreSQL драйвер, Flyway.
  Връзка към `business_db` (общия docker-compose Postgres на `localhost:5433`,
  user/pass `rezerv`/`rezerv`, override през env `DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD`).
  `ddl-auto: validate`, `open-in-view: false`.
- **Миграция `V1__nomenclatures_cities_and_categories.sql`**: таблици `cities` (id, name,
  slug UK) и `service_categories` (id, name, slug UK, icon) + seed: **27-те областни града**
  (28 области; София е център на две) и 8 начални категории (Фризьор, Барбер,
  Маникюр и педикюр, Масаж, Козметика, Грим, Вежди и мигли, Депилация).
- **Entities** `City`, `ServiceCategory` (Lombok по конвенцията), repositories,
  `NomenclatureService`, `NomenclatureController`.
- **`ContextHeaderFilter` + `ContextHeaders`** — слага `correlationId`/`userId` от
  gateway headers в MDC (същият като в rezerv-cas).
- **Тестове**: unit (Mockito) за service, `@WebMvcTest` за controller,
  context тест срещу реален PostgreSQL през **Testcontainers** (`@ServiceConnection`) —
  проверява и че Flyway миграциите минават.

## Решения
- Slug-овете са латиница за чисти URL-и (`/salons/sofia/masazh`) и локално SEO.
- `icon` е текстов код, който frontend-ът мапва към икона (не пазим SVG в базата).
- Сортиране по име в SQL заявката (`findAllByOrderByNameAsc`) — стабилен ред за dropdown-и.

## Как се тества
```bash
docker compose -f ../infra/docker-compose.yml up -d   # ако не върви
./mvnw verify                                          # unit + Testcontainers
./mvnw spring-boot:run
curl http://localhost:8082/business/public/cities      # 27 града
curl http://localhost:8082/business/public/categories  # 8 категории
```

## За frontend-а
Нови публични ендпойнти (през gateway: префикс `/api`):
- `GET /api/business/public/cities` → `[{ id, name, slug }]` — за "Къде?" полето.
- `GET /api/business/public/categories` → `[{ id, name, slug, icon }]` — за категорийните
  плочки; `icon` кодове: scissors, razor, nail-polish, massage, face, brush, eye, wax.
