# feature/salon-requires-company-approved

## Цел
Обект да се добавя само към одобрена фирма.

## Направено
- `createSalon` → 409 `COMPANY_NOT_APPROVED` ако status ≠ APPROVED
- Unit + integration тестове обновени

## Решения
UI вече блокира бутона; backend enforcement срещу директен API call.

## Как се тества
```bash
./mvnw -q -Dtest='CompanyOnboarding*' test
```
