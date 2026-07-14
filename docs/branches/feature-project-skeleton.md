# feature/project-skeleton

## Цел
Базов Spring Boot скелет на rezerv-business, върху който ще се гради бизнес логиката.

## Направено
- `pom.xml` — Spring Boot **3.5.16** (parent), Java **21**, Maven wrapper (`./mvnw`).
  Dependencies: `spring-boot-starter-web`, `spring-boot-starter-validation`,
  `spring-boot-starter-actuator`, Lombok (optional), `spring-boot-starter-test`.
- `application.yml` — име `rezerv-business`, порт **8082**, **virtual threads включени**,
  actuator: само `health` + `info`.
- Main class `bg.rezerv.business.RezervBusinessApplication`.
- Smoke тест `RezervBusinessApplicationTests.contextLoads()` — контекстът стартира.

## Решения
- Без JPA/PostgreSQL/Flyway в скелета — добавят се с първата реална entity/миграция,
  за да не изисква стартирането локална база още от ден 1.
- Base package: `bg.rezerv.business`.
- Версия 3.5.16 = последната стабилна Boot 3.x (планът фиксира Boot 3.x).

## Как се тества
```bash
./mvnw verify          # build + тестове
./mvnw spring-boot:run # старт на :8082
curl http://localhost:8082/actuator/health
```

## За frontend-а
Още няма бизнес ендпойнти. Наличен е само `GET /actuator/health` (директно на :8082, не през gateway).
