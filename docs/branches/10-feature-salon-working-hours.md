# feature/salon-working-hours

## Цел
Задължително работно време по дни при създаване на обект (различни часове напр. за събота).
Wipe на стари фирми/обекти без часове.

## Направено
- Flyway **V8**: таблица `working_hours` (salon_id, staff_id NULL = обект, day_of_week 1–7 ISO,
  start/end). TRUNCATE на companies/salons/… + демо seed с пн–пет 09–18 и съб 09–14.
- `POST /business/companies/{id}/salons` изисква `workingHours[]` (непразен).
- `PUT /business/salons/{id}/working-hours` — пълна смяна на графика на обекта.
- `GET /companies/mine` връща `workingHours` към всеки салон.
- Валидация: ≥1 ден, уникални дни, open < close.
- Unit тестове за validator + обновени onboarding тестове.

## Решения
- ISO ден: 1=пн … 7=нд (като `java.time.DayOfWeek`).
- `staff_id` колоната е подготвена за бъдещи смени на служители; в този branch само salon-level.
- CAS `user_companies` / `users.company_id` се чистят ръчно при deploy (виж по-долу) —
  няма Flyway в CAS за wipe.

## Как се тества
```bash
./mvnw -q test
# След старт на business (Flyway V8):
# docker exec rezerv-postgres psql -U rezerv -d cas_db \
#   -c "TRUNCATE user_companies; UPDATE users SET company_id = NULL;"
```

## За frontend-а
- Create salon body: `workingHours: [{ dayOfWeek, openTime, closeTime }]` (LocalTime ISO `HH:mm`).
- `PUT /api/business/salons/{salonId}/working-hours` с `{ workingHours: [...] }`.
- Mine companies: всеки салон има `workingHours[]`.
