# Flatmate

Person-to-person flatmate matching. Email + password login, a personal profile
dashboard, an explainable match score, consent-gated chat.

## Run it

One command (builds nothing, starts API + public tunnel):

```bash
./run.sh            # API + public HTTPS URL
./run.sh --build    # rebuild the UI first, then start
```

The API serves the built React app from `backend/src/main/resources/static`,
so everything is one origin: **http://localhost:8080**.

For UI development with hot reload:

```bash
cd frontend && npm run dev      # http://localhost:5173 -> API on :8080
```

- `http://localhost:8080/swagger-ui/index.html` — API docs, click Authorize to try calls

**Demo accounts** (all password `secret123`):

| Email | City |
|---|---|
| asha@example.com | Chandigarh / Mohali |
| bilal@example.com | Chandigarh / Sector 17 |
| esha@example.com | Chandigarh / Sector 22 |
| farhan@example.com | Chandigarh / Sector 35 |
| gita@example.com | Chandigarh / Panchkula |
| chetan@example.com | Ludhiana / Model Town |

Re-seed any time: `node backend/scripts/seed.mjs`

**Status:** complete. Backend + frontend both working, all 10 steps done.

Verify the backend any time:
```bash
cd backend && mvn test                                # 8 unit tests
node scripts/stomp-test.mjs <tokB> <convId> <tokA>    # 7 realtime + auth checks
```

**Cities:** Chandigarh, Ludhiana, Amritsar, Jalandhar, Delhi, Mumbai, Pune, Hyderabad.

**Known gotchas already hit (do not repeat):**
- Locality is a *score* factor, not a filter — chained adjacency excluded everyone
- `CHAR(36)` / `TINYINT` columns need `@JdbcTypeCode(SqlTypes.CHAR/TINYINT)`
- `INT UNSIGNED` columns map to `Integer`, not `Long`
- `@Transactional` + `throw` rolls back counters — use `noRollbackFor = ApiException`
- STOMP topics have **no** authorization by default — the interceptor is mandatory
- Timestamps: JVM forced to UTC + `connectionTimeZone=UTC`, frontend appends `Z`. Mixing these silently offsets every message by 5:30.


## Stack

| Layer | Choice |
|---|---|
| Backend | Spring Boot 3.5.16 · Java 21 target (runs on JDK 25) |
| DB | MySQL 8.4 · Flyway · utf8mb4 |
| Auth | Email + BCrypt → JWT access (15 min) + refresh (30 d) |
| Realtime | STOMP over WebSocket |
| Frontend | React + Vite + TypeScript + TanStack Query |
| Tests | JUnit 5 + Testcontainers (mysql:8.4) |

## Layout

```
flatmate/
├── backend/
│   ├── pom.xml
│   ├── scripts/db-setup.sql
│   └── src/main/
│       ├── java/in/flatmate/
│       └── resources/
│           ├── application.yml
│           └── db/migration/
│               ├── V1__init.sql
│               └── V2__seed_cities_areas.sql
└── frontend/            (not started)
```

## Run

```bash
# 1. database (once)
mysql -u root < backend/scripts/db-setup.sql

# 2. start api — Flyway applies V1 + V2 on boot
cd backend && mvn spring-boot:run
```

Health check: `curl localhost:8080/actuator/health` (added in step 3).

## Build order

- [x] 1. Schema (`V1__init.sql`)
- [x] 1b. Seed data (`V2__seed_cities_areas.sql`)
- [x] 2. Spring project + Flyway wiring
- [x] 3. SecurityConfig + JWT + email/password auth
- [x] 4. Profile onboarding endpoints
- [x] 5. MatchingService + weights + unit tests (8 passing)
- [x] 6. Match list + request/accept/decline
- [x] 6b. Conversation created on accept
- [x] 7. STOMP config + SubscriptionAuthInterceptor
- [x] 8. Chat send/history/read
- [x] 9. Rate limit (Bucket4j) + link detector
- [x] 10. React app
- [x] 11. Email/password auth (OTP removed)
- [x] 12. Personal profile dashboard (education, age, birthplace, occupation)
- [x] 13. Punjab + metro city seed (8 cities, 45 areas)
- [x] 14. UI redesign (sidebar shell, score rings, glass surfaces)
