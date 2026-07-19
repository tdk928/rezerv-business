# feature/internal-availability

## Цел
Internal API за rezerv-booking: работно време, closures, staff+услуги+time-off.

## Направено
- `GET /internal/salons/{id}/availability-context?from&to`
- `GET /internal/salons/{id}/actors/{userId}` → owner / staffMemberId
- Unit тестове

## За booking
Един call за целия хоризонт на слотовете.
