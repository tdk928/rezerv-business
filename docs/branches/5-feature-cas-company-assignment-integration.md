# 5-feature-cas-company-assignment-integration

## Цел
След успешен `POST /business/companies` rezerv-business да вика rezerv-cas internal endpoint,
за да зададе `company_id` + роля `BUSINESS_OWNER` на owner-а. При failure — rollback на транзакцията.

## Направено
- **`CasProperties`** — `rezerv.cas.base-url` (default `http://localhost:8081`, env `CAS_URI`).
- **`CasClientConfig`** — `RestClient` bean към CAS.
- **`CasClient`** — `POST /internal/users/{userId}/assign-company` с body `{ "companyId": long }`.
  При network/HTTP грешка → `502 CAS_ASSIGN_FAILED`.
- **`CompanyOnboardingService.registerCompany()`** — след INSERT в `companies` вика `casClient.assignCompany()`.
- Тестове: mock на `CasClient` в unit + integration (`@MockitoBean`).

## Решения
- **Synchronous call** в същата `@Transactional` — ако CAS fail-не, company INSERT се rollback-ва.
- **Без retry** — onboarding е рядък; user може да опита отново.
- Frontend след успех трябва **`POST /api/auth/refresh`** за нов JWT с `companyId` + `BUSINESS_OWNER`.

## Договор с rezerv-cas
Вече имплементиран в `feature/company-assignment-internal` (merged в cas `development`):

```
POST http://localhost:8081/internal/users/{userId}/assign-company
{ "companyId": 123 }
→ 200 UserResponse | 404 | 409 | 403
```

## Как се тества
```bash
# unit + integration (CasClient mocked)
./mvnw verify

# end-to-end (cas + business running):
# 1. Register/login → JWT
# 2. POST /api/business/companies с Authorization
# 3. POST /api/auth/refresh → JWT с companyId
# 4. GET /api/auth/me → roles ["BUSINESS_OWNER","CLIENT"]
```
