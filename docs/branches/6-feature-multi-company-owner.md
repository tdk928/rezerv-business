# 6-feature-multi-company-owner

## Цел
Owner може да регистрира **много фирми** (по ЕИК). Премахнато ограничението
„1 user = 1 company“.

## Направено
- Махнат check `existsByOwnerUserId` / `COMPANY_ALREADY_REGISTERED`.
- **`GET /business/companies/mine`** → списък `CompanyResponse` за текущия `X-User-Id`
  (сортирани по `created_at`).
- Запазени: unique EIK, owner check при салони, CAS `assignCompany` след create.
- Включени и предишни локални промени: ЕИК само 9 цифри (V4), salon email+phone
  задължителни (V5).

## Решения
- Ownership остава `companies.owner_user_id` (1 ред = 1 фирма; много редове на user).
- Активната фирма за JWT се управлява в CAS (`switch-company`), не тук.

## Как се тества
```bash
./mvnw verify

# Две фирми с един JWT:
POST /api/business/companies  # фирма 1
POST /api/business/companies  # фирма 2 (друг ЕИК) — трябва 200
GET  /api/business/companies/mine
```

## За frontend-а
| Method | Path | Бележка |
|--------|------|---------|
| GET | `/api/business/companies/mine` | за switcher |
| POST | `/api/business/companies` | може много пъти (различен ЕИК) |
