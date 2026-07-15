# 4-feature-company-salon-onboarding

## Цел
B2B onboarding slice 1: създаване на фирма, салон, услуга и снимка през protected API
(не seed), с auth от gateway headers и валидация на ЕИК.

## Направено
- **`RequestContext`** + разширен `ContextHeaderFilter` — чете `X-User-Id`, `X-User-Roles`,
  `X-Company-Id` от gateway (без JWT parsing в business).
- **`EikValidator`** — 9/13 цифри + българска контролна сума.
- **`CompanyRepository`** — `existsByEik`, `existsByOwnerUserId`, `findByIdAndOwnerUserId`.
- **`CompanyOnboardingService`** + controllers:
  - `POST /business/companies` — регистрация на фирма (`PENDING_APPROVAL`, owner = header user)
  - `POST /business/companies/{companyId}/salons` — салон (cityId от номенклатура)
  - `POST /business/salons/{salonId}/services` — услуга (categoryId от номенклатура)
  - `POST /business/salons/{salonId}/photos` — URL на снимка (+ optional position)
- Request/response **records** с Jakarta Validation.
- **Auth правила**: JWT задължителен (401 без `X-User-Id`); owner match (403); 1 фирма на user;
  unique EIK (409); invalid EIK (400).
- **Тестове**: `EikValidatorTest`, `CompanyOnboardingServiceTest`, `@WebMvcTest`
  `CompanyOnboardingControllerTest`, `@Transactional` `CompanyOnboardingIntegrationTest`
  (full flow).

## Решения
- **Flyway V3 не е нужна** — V2 schema покрива всички полета.
- Onboarding позволява всеки **authenticated** user без фирма (обикновено CLIENT след register).
  Роля `BUSINESS_OWNER` + `companyId` в JWT **не се задават тук** — виж договора по-долу.
- Demo seed EIK `204815936` има невалидна контролна сума (само dev данни); API-то го отхвърля
  при нов onboarding.
- Gateway routing вече покрива `/api/business/**` (JWT) vs `/api/business/public/**` — без промяна.

## Договор с rezerv-cas (следващ branch — НЕ е имплементирано тук)

След успешен `POST /api/business/companies` frontend/cas трябва да:
1. Добави роля **`BUSINESS_OWNER`** на user-а (запазвайки `CLIENT` ако е нужно).
2. Запише **`company_id`** на user-а = id на новата фирма.
3. При следващ login/refresh JWT claim **`companyId`** → gateway → `X-Company-Id`.

Дотогава B2B create endpoints работят само с `X-User-Id` + owner check в business_db.

## Как се тества
```bash
./mvnw verify

# С JWT през gateway (пример):
curl -X POST http://localhost:8080/api/business/companies \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"eik":"131529327","name":"Моя фирма","legalName":"Moya firma EOOD"}'
```

## За frontend-а (B2B панел — следваща фаза)

| Method | Path (през gateway) | Body |
|--------|---------------------|------|
| POST | `/api/business/companies` | `{ eik, name, legalName }` → `CompanyResponse` |
| POST | `/api/business/companies/{companyId}/salons` | `{ name, description?, cityId, address, lat?, lng?, phone? }` |
| POST | `/api/business/salons/{salonId}/services` | `{ categoryId, name, durationMin, price }` |
| POST | `/api/business/salons/{salonId}/photos` | `{ url, position? }` |

Грешки: `UNAUTHORIZED`, `EIK_INVALID`, `EIK_ALREADY_REGISTERED`, `COMPANY_ALREADY_REGISTERED`,
`NOT_COMPANY_OWNER`, `CITY_NOT_FOUND`, `CATEGORY_NOT_FOUND`, `SALON_NOT_FOUND`.
