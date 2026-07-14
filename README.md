# rezerv-business

Микросервиз от платформата **REZERV** (онлайн резервации за салони за красота /
бръснарници в България). Отговаря за: фирми (по ЕИК), салони, услуги + категории,
служители, работно време, снимки, номенклатури (градове, категории) и публичното търсене.

- **Порт:** 8082
- **Стек:** Java 21, Spring Boot 3.x (MVC, virtual threads), Maven, PostgreSQL (`business_db`), Flyway
- **Достъп:** само през `rezerv-gateway` (:8080) — `/api/business/**`; `/internal/**` е service-to-service

## Стартиране (dev)

```bash
./mvnw spring-boot:run
```

## Git workflow

`feature/* → development → test → main`

История на направеното: [docs/PROJECT_LOG.md](docs/PROJECT_LOG.md)
