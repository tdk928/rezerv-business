# feature/create-staff-onboarding

## Цел
Owner да може да създаде нов CAS потребител (STAFF) и да го закачи за обект/фирма,
в допълнение към съществуващото свързване по email.

## Направено
- `POST /business/salons/{salonId}/staff/create` — body: email, password, firstName,
  lastName, phone?, displayName?, title?.
- `CasClient.createStaffUser` → CAS `POST /internal/users/create-staff`.
- Persist в `staff_members` (същият модел като при link-by-email).
- Unit тест: `createStaffProvisionsCasUserAndPersists`.

## Решения
- Създаването на акаунт е в CAS; business само оркестрира + записва staff ред.
- 409 `EMAIL_ALREADY_EXISTS` от CAS се пренася към owner UI.
- displayName по подразбиране = firstName + lastName.

## Как се тества
```bash
./mvnw -q test -Dtest=StaffManagementServiceTest
```

## За frontend-а
`POST /api/business/salons/{salonId}/staff/create` (Bearer JWT на owner):
```json
{
  "email": "staff@example.com",
  "password": "secret123",
  "firstName": "Иван",
  "lastName": "Иванов",
  "phone": "+359...",
  "title": "Гримьор"
}
```
Отговор: `StaffMemberResponse` (id, userId, displayName, title, serviceIds, …).
Съществуващият `POST .../staff` (link by email) остава непроменен.
