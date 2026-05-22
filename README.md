# Arrive Competitor Intelligence

A 2-hour hackathon cut of a marketing-facing tool that ranks Arrive's organic
SEO posture against a small set of competitor domains. The marketing user adds
competitors, hits **Refresh**, and gets a side-by-side benchmark (organic
keywords / traffic / cost / top-10) plus a per-competitor keyword-gap table
sourced from SEMRush. A Spring Boot + Kotlin backend persists snapshots to
Postgres; Vaadin 25 renders the UI; a `FakeSemRushClient` is used by default so
we don't burn through the team's monthly SEMRush credit budget during dev.

## ⚠️ SEMRush credit budget

50,000 credits per month — **for the whole team**. The defaults in this repo
are configured to use the fake client. Don't flip `SEMRUSH_LIVE=true` except
for the live demo or a deliberate, reviewed smoke test. See
[TEAM_HANDOFF.md](TEAM_HANDOFF.md) for the full rules.

## Running it

### Local dev (recommended)

Requires JDK 21 and a container runtime (podman + podman-compose preferred;
docker works too — see [TEAM_HANDOFF.md](TEAM_HANDOFF.md) for install commands).

```bash
cp .env.example .env                 # leave SEMRUSH_API_KEY blank; fake client kicks in
podman-compose up -d postgres        # set POSTGRES_PORT=5433 in .env if 5432 is taken
./gradlew bootRun                    # first run downloads Vaadin frontend (~1–2 min)
open http://localhost:8080
```

### Everything in containers

After your Dockerfile builds locally:

```bash
podman-compose --profile app up --build
# app:      http://localhost:8080
# postgres: localhost:5432 (or POSTGRES_PORT)
```

The `app` service is gated behind the `app` compose profile so the postgres
container can run alone during dev without rebuilding the app image on every
change.

## Smoke test

`scripts/smoke.sh` exercises the happy path end-to-end against a running
instance. Run it after `bootRun` or `podman-compose --profile app up`:

```bash
./scripts/smoke.sh                   # defaults to http://localhost:8080
BASE_URL=http://staging:8080 ./scripts/smoke.sh
```

It creates two competitors, refreshes one, asserts `/api/benchmark` returns
Arrive in `own`, checks the keyword-gap endpoint, and cleans up. Requires
`curl` and `jq`. Exits 0 on success.

## What's in this cut

- Add / list / delete competitors (one of them flagged `isOwn=true` — Arrive).
- Per-competitor SEMRush refresh (organic ranks + MISSING/UNTAPPED keyword gap).
- Side-by-side benchmark grid pinned to Arrive.
- 24h snapshot cache so a re-refresh inside the window is free.
- REST API at `/api/*` for the marketing user's curl scripts.
- Fake SEMRush client by default — flip `SEMRUSH_LIVE=true` only for the demo.

## What's not in this cut

- **LinkedIn / Facebook / Instagram / TikTok mentions.** SEMRush's public API
  has no documented endpoint for them; confirmed with marketing at planning.
- Sentiment analysis.
- Auth, multi-tenant, history charts, scheduled / background refresh.
- Anything visually polished beyond the demo path (add → refresh → numbers).

## Layout

| Package | Owner stream | Purpose |
| --- | --- | --- |
| `contracts/` | shared (frozen) | DTOs every workstream codes against |
| `domain/`, `repo/`, `db/migration/` | P1 | JPA entities, repos, Flyway migrations |
| `semrush/` | P2 | `SemRushClient` interface + Fake/Real impls |
| `service/`, `api/` | P3 | Business logic, REST controllers |
| `ui/` | P4 | Vaadin Flow views |
| `Dockerfile`, `docker-compose.yml`, `scripts/` | P5 | DevOps + smoke |

See [TEAM_HANDOFF.md](TEAM_HANDOFF.md) for per-stream acceptance criteria and
the SEMRush API gotchas.

## Screenshot

_TODO: attach once P4's MainView is live._
