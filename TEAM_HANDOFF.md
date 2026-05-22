# Team Handoff — Arrive Competitor Intelligence

5 people, 2 hours. The baseline is landed and ready to fork from. **Pick your stream and start.**

## ⚠️ HARD CONSTRAINT — SEMRush Credit Budget

> **We have 50,000 SEMRush credits this month for the whole team. Use them very, very reasonably.**

Practical rules baked into the codebase — follow them:

- **Default is the fake client.** `SEMRUSH_LIVE=false` in `.env.example`. Don't flip it during dev. The `FakeSemRushClient` already returns realistic-looking data via `@ConditionalOnMissingBean`, so end-to-end demos work without burning a single credit.
- **`semrush.live=true` only for**: (a) the live final demo, (b) one deliberate smoke test per workstream after the code is reviewed.
- **Cache aggressively.** `semrush.cache-ttl-hours=24` — P2/P3 must consult the persisted `DomainMetricsSnapshot` / `KeywordGapRow` before calling SEMRush. If the latest row is < 24h old, reuse it.
- **Cap result size.** `semrush.gap-row-limit=25`. `domain_domains` charges ~40 credits per returned row — do not ask for hundreds.
- **No background/scheduled refresh.** Refresh runs ONLY when a user clicks the Refresh button.
- **Confirm before refresh-all.** P4: the "Refresh all" button must show a confirmation dialog with an estimated credit cost (`competitors × (10 + 2 × gap_row_limit × 40)`).
- **Rough cost per full refresh** at 4 competitors + Arrive: ~3,200 credits. Budget allows ~15 full refreshes/month — assume teammates demoing also use credits.

If you're about to call SEMRush in a loop, stop and ask the team.

## What's already done (Phase 0)

- **Gradle deps wired**: Spring Boot 4.0.6, Kotlin 2.2.21, JPA, Postgres driver, Flyway, Spring Web/Validation, **Vaadin 25.1.5**. Run `./gradlew compileKotlin` — it builds clean.
- **Postgres in a container**: `docker-compose.yml` runs `postgres:16-alpine` with a healthcheck.
- **Shared DTO contracts** live in `src/main/kotlin/.../contracts/`. Every workstream codes against these.
- **`SemRushClient` interface** + **`FakeSemRushClient`** registered via `@ConditionalOnMissingBean`. P3/P4 can build end-to-end **right now** without waiting for P2; when P2's real client lands, the fake silently steps aside.
- **`application.properties`** wired to Postgres + Flyway + SEMRush key. `application-docker.properties` overrides for compose.
- **Package skeleton** created with `_workstream.kt` markers showing who owns what — delete those files as you add real code.

## Start here (every teammate, one minute)

```bash
cp .env.example .env
# Edit .env: leave SEMRUSH_API_KEY blank for now (FakeSemRushClient kicks in).
# If 5432 is taken on your laptop, set POSTGRES_PORT=5433 (or any free port).

podman-compose up -d postgres        # native podman-compose works; `podman compose` wrapper has socket issues
./gradlew bootRun                    # first Vaadin run downloads frontend assets (~1-2 min, be patient)
# http://localhost:8080  — empty until P4 ships MainView
```

> Note: the `docker` CLI on this team's machines is fronted by podman but mis-routed to a broken socket. Use **`podman-compose`** directly (not `podman compose` or `docker compose`).

## Shared contracts — code against these

All in `com.arrive.ai_training_5th_may_breakout_2.contracts`:

| Type | Purpose |
| --- | --- |
| `CompetitorDto(id, name, domain, isOwn)` | Competitor in JSON / Vaadin grid |
| `CreateCompetitorRequest(name, domain, isOwn)` | POST body |
| `DomainMetricsDto(domain, organicKeywords, organicTraffic, organicCost, top10Keywords)` | What SEMRush gives us per domain |
| `KeywordGapRowDto(keyword, gapType, volume, kd, positionBase, positionCompetitor, cpc)` | One row of the gap table |
| `GapType` | `MISSING / UNTAPPED / SHARED / UNIQUE` |
| `MetricRow(competitor, metrics, fetchedAt)` | One row of the benchmark grid |
| `BenchmarkResponse(own, competitors)` | Payload of `GET /api/benchmark` |

Don't change these without telling the other workstreams.

## Workstreams

### P1 — Persistence & Migrations (75 min)
**Files you own:** `domain/`, `repo/`, `src/main/resources/db/migration/`.
- Replace `V0__placeholder.sql` with real migrations: `V1__competitor.sql`, `V2__domain_metrics_snapshot.sql`, `V3__keyword_gap_row.sql` (delete the V0).
- Entities: `Competitor`, `DomainMetricsSnapshot`, `KeywordGapRow` (Kotlin `@Entity`, the `kotlin-jpa` plugin is already wired so no-arg constructors are generated for you).
- Repositories with two finders that actually matter:
  - `DomainMetricsRepository.findTopByCompetitorIdOrderByFetchedAtDesc(id): DomainMetricsSnapshot?`
  - `KeywordGapRepository.findAllByCompetitorIdAndGapType(id, gapType): List<KeywordGapRow>`
  - and `KeywordGapRepository.deleteAllByCompetitorId(id)` for refresh-overwrite.
- Schema lives in your migrations; `spring.jpa.hibernate.ddl-auto=validate` will yell if entities drift.
- **Acceptance:** app boots, schema visible in Postgres (`podman exec arrive-intel-postgres psql -U arrive arrive_intel -c '\dt'`), `CompetitorRepository.save` round-trips.

### P2 — SEMRush Client (75 min)
**Files you own:** everything in `semrush/` except `SemRushClient.kt` (interface, frozen) and `FakeSemRushClient.kt`/`SemRushClientConfig.kt` (leave as fallback).
- Add `RealSemRushClient` as `@Component` — `@ConditionalOnMissingBean` on the fake means it auto-disables once your bean exists.
- Use Spring `RestClient` (already on classpath via `spring-boot-starter-web`).
- API key already injected: `@Value("\${semrush.api.key:}") apiKey: String`. If blank, throw on construction so Spring picks the fake instead.
- Endpoints (SEMRush returns semicolon-delimited CSV — hand-roll a tiny splitter, no extra dep):
  - `type=domain_ranks&domain=…&database=us` → `DomainMetricsDto`
  - `type=domain_domains&domains=*|or|<base>|+|or|<competitor>` (shared), `+|or|<base>|-|or|<competitor>` (unique to base), etc. Translate `MISSING` and `UNTAPPED` to the correct sign pairs — see [SEMRush API docs](https://developer.semrush.com/api/seo/domain-reports/).
- Unit-test the CSV parser against two canned fixtures. Integration test with `@EnabledIfEnvironmentVariable(named = "SEMRUSH_API_KEY", matches = ".+")` for live calls.
- **Acceptance:** with `SEMRUSH_API_KEY` set in `.env`, `RealSemRushClient.fetchDomainRanks("arrive.com")` returns non-zero metrics.

### P3 — Service layer + REST API (75 min)
**Files you own:** `service/`, `api/`.
- `CompetitorService`: CRUD on `Competitor`. **Enforce at most one `isOwn=true`** — service-level guard, throw on conflict (the schema doesn't enforce this).
- `RefreshService`: takes `competitorId`, calls `SemRushClient.fetchDomainRanks` for both Arrive's domain and the competitor's, persists snapshots; calls `fetchKeywordGap` for MISSING + UNTAPPED, deletes old gap rows then inserts new ones.
- `BenchmarkService`: assembles `BenchmarkResponse` from repos (latest metric snapshot per competitor).
- Controllers under `/api`:
  ```
  GET    /api/competitors
  POST   /api/competitors                          { name, domain, isOwn }
  DELETE /api/competitors/{id}
  POST   /api/competitors/{id}/refresh
  POST   /api/refresh-all
  GET    /api/competitors/{id}/keyword-gap?type=MISSING
  GET    /api/benchmark
  ```
- **Acceptance:** `curl -X POST localhost:8080/api/competitors -d '{"name":"Arrive","domain":"arrive.com","isOwn":true}' -H 'content-type: application/json'` then `curl localhost:8080/api/benchmark | jq` returns Arrive in `own`.

### P4 — Vaadin UI (90 min, highest risk — get started first)
**Files you own:** `ui/`.
- Vaadin 25 with Flow. The `vaadin-spring-boot-starter` is already on the classpath. First run downloads frontend assets — be patient.
- `MainView` at `@Route("")`, uses `AppLayout`:
  - **Header**: title "Arrive Competitor Intelligence", `Button("Add competitor")` (opens dialog), `Button("Refresh all")` (calls `RefreshService.refreshAll()`).
  - **Body top**: `Grid<MetricRow>` bound to `BenchmarkService.benchmark()`. Pin Arrive (the `isOwn` row) first; visually highlight it (`grid.classNameGenerator`).
  - **Body bottom**: `Tabs` — one tab per competitor — each tab swaps a `Grid<KeywordGapRow>` showing MISSING rows. Bonus: a sub-toggle for MISSING / UNTAPPED.
- `AddCompetitorDialog`: `TextField` name, `TextField` domain, `Checkbox("This is us (Arrive)")`. Save → `competitorService.create(CreateCompetitorRequest(...))` → close dialog → refresh grid.
- **Inject services directly** — no need to hit the REST API from Vaadin Flow. The REST API is for the marketing user's curl/scripts, not the UI.
- **Acceptance:** open `http://localhost:8080`, add Arrive + 2 competitors, click "Refresh all", see populated grid and at least one keyword tab.

### P5 — DevOps + demo polish + floater (90 min)
**Files you own:** `Dockerfile`, `docker-compose.yml` (app service), `README.md`, smoke test.
- **Dockerfile** — multi-stage: `gradle:8-jdk21` stage runs `./gradlew bootJar -Pvaadin.productionMode`; runtime stage `eclipse-temurin:21-jre`, `COPY` the jar, `ENTRYPOINT java -jar app.jar`.
- The `app` service in `docker-compose.yml` is already defined but gated behind the `app` profile so postgres can run alone right now. After your Dockerfile lands, document `podman-compose --profile app up --build`.
- `README.md`: 1-paragraph summary, "how to run", "what's in / what's out" (mention that LinkedIn/social was descoped from this 2h cut), and a screenshot once P4 is up.
- Once your DevOps work is done (~30 min), **float to whichever stream is bleeding** — most likely P4. Check in with everyone at the 45-min mark.

## Known issues / footguns

- **Podman socket**: `docker compose` and `podman compose` both fail with a "missing socket" error on this team's setup. Use `podman-compose` (the standalone Python binary). Resolved.
- **Port 5432 conflict**: another podman project (`cozadzban-postgres-1`) holds 5432 on at least one teammate's laptop. Set `POSTGRES_PORT=5433` in `.env` if you hit it — both the compose port mapping and Spring datasource URL read this env var.
- **Vaadin first run** takes 1–2 minutes downloading frontend bundles. Don't kill the build thinking it's stuck. Subsequent runs are fast.
- **Flyway + empty schema**: `V0__placeholder.sql` is a `SELECT 1` no-op so the app boots before P1 lands real migrations. P1: delete it before merging V1.
- **`isOwn` uniqueness** is enforced in `CompetitorService`, not the DB. Don't add the constraint to the migration — Postgres partial unique indexes on booleans are awkward and we don't need the trouble.

## Out of scope (don't accidentally build it)

- LinkedIn / Facebook / Instagram / TikTok mentions. **SEMRush's public API has no documented endpoint for these.** Confirmed with the marketing teammate at planning time.
- Sentiment analysis.
- Auth, multi-tenant, history charts, scheduled refresh.
- Anything pretty. The demo path is: add competitors → refresh → see numbers. Ship that, then polish.
