# 7-feature-owner-admin-company-lists

## Цел
Owner вижда своите фирми + обекти; platform admin вижда всички фирми със
собственици (име/email от CAS).

## Направено
- **`GET /business/companies/mine`** → `CompanyWithSalonsResponse[]` (фирма + nested `salons`).
- **`GET /business/admin/companies`** → `AdminCompanyResponse[]` (фирма + `owner`);
  само `PLATFORM_ADMIN` (403 иначе).
- `SalonRepository.findByCompanyIdInOrderByNameAsc`.
- `CasClient.lookupUsers` → CAS `POST /internal/users/lookup`.
- Unit тестове за mine+salons, admin guard, owner enrichment.

## Решения
- Enrich на `/mine` вместо отделен salons endpoint — един round-trip за UI.
- Owner PII не се дублира в business_db — resolve през CAS.
- Admin path е `/business/admin/companies` (не `/companies` с query) за ясен guard.

## Как се тества
```bash
./mvnw test

# Owner:
curl http://localhost:8080/api/business/companies/mine -H "Authorization: Bearer $TOKEN"

# Admin (admin@rezerv.bg):
curl http://localhost:8080/api/business/admin/companies -H "Authorization: Bearer $ADMIN"
```

## За frontend-а
| Method | Path | Auth | Response |
|--------|------|------|----------|
| GET | `/api/business/companies/mine` | JWT | фирми + `salons[]` |
| GET | `/api/business/admin/companies` | JWT + `PLATFORM_ADMIN` | фирми + `owner{id,email,firstName,lastName}` |
