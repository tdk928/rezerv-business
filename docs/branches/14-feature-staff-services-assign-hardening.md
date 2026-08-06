# feature/staff-services-assign-hardening

## Цел
Да се гарантира, че `PUT /business/staff/{id}/services` (абонамент на служител към
услуги на обекта) е надежден и покрит с unit тестове — endpoint-ът вече съществуваше
от `feature/staff-schedules-time-off`, но без тестове за replace и с риск при
delete+insert на същите PK в една транзакция.

## Направено
- `StaffServiceLinkRepository.deleteByStaffId` → `@Modifying(flushAutomatically,
  clearAutomatically)` + explicit `@Query`, за да се flush-не delete преди insert.
- `replaceStaffServices` връща `serviceIds` от току-що записаните линкове (без
  допълнителен read, който може да види стар persistence context).
- Unit тестове в `StaffManagementServiceTest`:
  - успешен replace (с distinct на дублирани ids)
  - `SERVICE_NOT_IN_SALON` при чужда услуга
  - празен списък изчиства всички линкове
  - `NOT_COMPANY_OWNER` без ownership

## Решения
- Не се сменя публичният контракт: `PUT /business/staff/{staffId}/services`
  body `{ "serviceIds": number[] }` → `StaffMemberResponse`.
- Логиката остава в `rezerv-business` (`StaffManagementService`).

## Как се тества
```bash
./mvnw -q test -Dtest=StaffManagementServiceTest
```

Ръчно през gateway (owner JWT):
```bash
curl -X PUT http://localhost:8080/api/business/staff/{staffId}/services \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"serviceIds":[16]}'
```
