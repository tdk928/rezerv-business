# feature/salons-public-search

## Цел
Салоните като данни (фирми, салони, снимки, услуги) + публичното търсене и детайлът
на салон — backend-ът, който храни списъка с резултати, "Топ салони" и страницата на салона.

## Направено
- **Миграция `V2__companies_and_salons.sql`**: таблици `companies` (ЕИК unique, CHECK
  9/13 цифри, статус PENDING_APPROVAL/APPROVED/SUSPENDED), `salons` (град FK, адрес,
  lat/lng, rating_avg/rating_count, статус ACTIVE/INACTIVE), `salon_photos`,
  `salon_services` (категория FK, duration_min, price, active) + индекси.
- **Демо seed**: 2 фирми, 6 салона (3 София, Пловдив, Варна, Бургас) със снимки
  (picsum.photos) и 14 услуги в различни категории — за да има живо съдържание.
- **Entities**: `Company`, `Salon`, `SalonPhoto`, `SalonServiceItem` (+ enums). Салонът
  държи `city` като @ManyToOne (трябва ни името/slug в отговорите), а `companyId` е
  обикновена колона (не навигираме натам).
- **Публични ендпойнти** (`SalonPublicController`):
  - `GET /business/public/salons` — филтри `cityId`, `categoryId` (EXISTS по активни
    услуги), `q` (по име, case-insensitive), `page`/`size` (max 50). Сортиране: рейтинг
    desc, брой отзиви desc. Картите включват главна снимка и "от X лв." (мин. цена).
  - `GET /business/public/salons/{id}` — детайл: снимки по позиция + услуги, групирани
    по категория; 404 `SALON_NOT_FOUND` в единния формат за грешки.
- **Error handling**: `ApiException` + `GlobalExceptionHandler` + `ErrorResponse`
  (същият шаблон като rezerv-cas, REZERV.md §2.6).
- **Тестове**: unit за `SalonQueryService` (карти/групиране/404), `@WebMvcTest` за
  контролера (вкл. формата на 404), интеграционен `SalonSearchIntegrationTest` с
  Testcontainers срещу реалните миграции + seed (филтри по категория и име).

## Решения
- `salon_services` entity-то се казва `SalonServiceItem`, за да не се бърка със Spring
  `@Service` анотацията/слоя.
- Главната снимка и мин. цената се взимат с 2 групирани заявки за страницата резултати
  (без N+1).
- Демо seed-ът е част от V2 за простота в dev; при реалния onboarding данните ще идват
  от B2B панела.

## Как се тества
```bash
./mvnw verify
./mvnw spring-boot:run
curl 'http://localhost:8082/business/public/salons?q=barber'
curl 'http://localhost:8082/business/public/salons/1'
```

## За frontend-а
- `GET /api/business/public/salons?cityId=&categoryId=&q=&page=&size=` →
  `{ content: [{ id, name, city:{id,name,slug}, address, ratingAvg, ratingCount, photoUrl, priceFrom }], page, size, totalElements, totalPages }`
- `GET /api/business/public/salons/{id}` →
  `{ id, name, description, city, address, lat, lng, phone, ratingAvg, ratingCount, photos:[url], serviceGroups:[{ categoryId, categoryName, categorySlug, services:[{ id, name, durationMin, price }] }] }`
- 404: `{ status:404, code:"SALON_NOT_FOUND", message, correlationId, timestamp }`
