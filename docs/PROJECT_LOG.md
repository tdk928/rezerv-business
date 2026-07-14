# PROJECT LOG — rezerv-business

Индекс на всички branches/PR-и (най-новите отгоре). Детайли за всеки branch:
`docs/branches/<branch-name>.md`.

| № | Branch | Дата | Обобщение |
|---|--------|------|-----------|
| 3 | feature/salons-public-search | 2026-07-14 | Фирми/салони/снимки/услуги (V2 + демо seed: 6 салона). Публично търсене `GET /api/business/public/salons` (филтри cityId/categoryId/q, карти със снимка и "от X лв.") + детайл `GET /api/business/public/salons/{id}` (услуги по категории). Единен error формат. |
| 2 | feature/nomenclatures-cities-categories | 2026-07-14 | PostgreSQL (business_db) + Flyway. Номенклатури: 27 областни града + 8 категории услуги. Публични ендпойнти: `GET /api/business/public/cities` и `GET /api/business/public/categories`. Unit + Testcontainers тестове. |
| 1 | feature/project-skeleton | 2026-07-14 | Spring Boot 3.5.16 скелет: Java 21, Maven wrapper, web/validation/actuator/Lombok, порт 8082, virtual threads, smoke тест. Без бизнес ендпойнти и без база още. |
