# feature/staff-schedules-time-off

## Цел
Служители на обект с собствен график (⊆ салона), абонамент за услуги, затваряне на ден,
time-off от owner + заявки от STAFF (approve/reject).

## Направено
- Flyway **V9**: `staff_members`, `staff_services`, `salon_closures`, `time_off`,
  `time_off_requests` + unique index за staff working hours.
- Owner API (JWT, ownership):
  - `POST/GET /business/salons/{salonId}/staff`
  - `PUT /business/staff/{id}/working-hours` (валидира ⊆ salon hours)
  - `PUT /business/staff/{id}/services`
  - `POST/GET/DELETE /business/salons/{salonId}/closures`
  - `POST/GET /business/staff/{id}/time-off` (owner директно затваря часове)
  - `GET .../time-off-requests`, `POST .../approve|reject`
- Staff API (роля STAFF):
  - `POST/GET /business/staff/me/time-off-requests`
- CAS: `findByEmail` + `assignStaff` през `CasClient`.

## Решения
- Служителят = CAS user с роля STAFF (регистрира се като CLIENT, owner го връзва по email).
- Постоянен график — само owner; отпуск — заявка от staff или директно от owner.
- Approve на заявка създава ред в `time_off` (source=REQUEST).

## Как се тества
```bash
./mvnw -q test
```

## За frontend-а
1. Регистрирай user (email) → owner: `POST .../staff` с `{ email, displayName?, title? }`.
2. Задай смяна: `PUT .../staff/{id}/working-hours`.
3. Абонирай услуги: `PUT .../staff/{id}/services` `{ serviceIds }`.
4. Затвори ден: `POST .../closures` `{ closedOn, reason? }`.
5. Time-off: owner `POST .../time-off` или staff `POST .../me/time-off-requests` → owner approve.
