# feature/mine-salons-with-services

## Цел
`GET /companies/mine` да връща услугите на всеки салон (за „Моите обекти“ UI).

## Направено
- `SalonResponse.services`
- Batch load `findBySalonIdInOrderByNameAsc`
- Unit тест обновен

## Как се тества
```bash
./mvnw -q -Dtest='CompanyOnboarding*' test
```
