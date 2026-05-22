# AGENTS.md — guidance for AI agents working on this repo

This file is read by AI coding assistants (Claude Code, Cursor, Aider, OpenAI Codex agents, etc.) before they make changes. Follow it.

## Project context

Hackathon project: **Arrive Competitor Intelligence**. Spring Boot 4.0.6 + Kotlin 2.2.21 + Java 21 + Vaadin 25 + PostgreSQL via Flyway/JPA. Five-person team, two-hour timebox. The implementation plan lives in `~/.claude/plans/we-re-5-people-running-vectorized-taco.md` and the per-person workstream split is in `TEAM_HANDOFF.md` — read both before editing.

## ⚠️ HARD CONSTRAINT — SEMRush credit budget

> **The team has 50,000 SEMRush credits this month. Use them very, very reasonably.**

If your change touches `semrush/` or anything that might call the SEMRush API:

1. **The default client is `FakeSemRushClient`.** Never change that default. Real calls are gated on `semrush.live=true` (env var `SEMRUSH_LIVE`).
2. **Never auto-trigger a refresh** (on app start, on grid render, on schedule, on competitor add, on test run). Refreshes only happen on explicit user action.
3. **Honor the cache.** Before calling SEMRush, check the persisted snapshot. If newer than `semrush.cache-ttl-hours` (default 24), reuse it.
4. **Cap row counts.** `semrush.gap-row-limit` (default 25) constrains `domain_domains` requests. `domain_domains` charges ~40 credits **per row** — do not raise the limit without explicit team approval.
5. **No loops over competitors that fire real API calls** without a team conversation first.
6. **Tests must use the fake client.** Never write a test that reads `SEMRUSH_API_KEY` and calls the live API by default. Live integration tests must be opt-in via `@EnabledIfEnvironmentVariable`.
7. **If you can't tell whether your change is safe**, leave `semrush.live` defaulted to `false` and ask the user.

## Build & run

```bash
# Bring up Postgres (use podman-compose, NOT `docker compose` / `podman compose` — they fail on this team's socket setup)
cp .env.example .env
podman-compose up -d postgres

# Build / compile
./gradlew compileKotlin

# Run the app (first run downloads Vaadin frontend assets — slow, that's normal)
./gradlew bootRun
```

If port 5432 is taken locally, set `POSTGRES_PORT=5433` in `.env` (both Postgres and Spring read it).

## Architecture (single-module package layout)

```
com.arrive.ai_training_5th_may_breakout_2
├── contracts  — shared DTOs every layer codes against; do not break these
├── domain     — JPA entities (P1's territory)
├── repo       — Spring Data repositories (P1)
├── semrush    — SemRushClient interface + Fake + Real impl (P2)
├── service    — CompetitorService, RefreshService, BenchmarkService (P3)
├── api        — REST controllers under /api (P3)
└── ui         — Vaadin Flow views, AppLayout (P4)
```

## Conventions

- Kotlin throughout. `kotlin-spring` and `kotlin-jpa` (no-arg) plugins are enabled — `@Entity` classes can be Kotlin data classes / regular classes.
- Persistence schema is owned by Flyway in `src/main/resources/db/migration/`. JPA is `ddl-auto=validate`; if your entity drifts from the schema, the app refuses to start.
- The `isOwn` uniqueness ("only one Arrive row") is enforced in the **service layer**, not the database. Don't add a unique partial index for it.
- Vaadin views inject services directly (no HTTP round-trip from UI to controller). REST controllers exist for the marketing user's curl/scripts.

## Out of scope (do not implement)

- LinkedIn / Facebook / Instagram / TikTok mentions or sentiment. SEMRush's public developer API has **no documented endpoint** for social mentions. Confirmed with the product owner. Adding it requires a different data source and a separate sprint.
- Auth, multi-tenant, historical charts, scheduled refresh.

## When in doubt

Ask the user before:
- Calling live SEMRush in any new code path.
- Changing anything in `contracts/` (other workstreams depend on it).
- Adding background tasks, schedulers, or anything that runs without an explicit click.
- Adding new top-level dependencies to `build.gradle.kts`.
