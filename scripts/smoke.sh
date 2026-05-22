#!/usr/bin/env bash
#
# End-to-end smoke test for Arrive Competitor Intelligence.
# Assumes the app is reachable at $BASE_URL (defaults to http://localhost:8080)
# and is running with the FakeSemRushClient (no SEMRush credits burned).
#
# Usage:
#   ./scripts/smoke.sh                 # hits http://localhost:8080
#   BASE_URL=http://host:8080 ./scripts/smoke.sh
#
# Exit code 0 = all green, non-zero = first failed step.

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
TIMEOUT="${TIMEOUT:-90}"      # seconds to wait for boot
CURL="curl -sS --max-time 10"

pass() { printf '  \033[32mPASS\033[0m %s\n' "$1"; }
fail() { printf '  \033[31mFAIL\033[0m %s\n' "$1" >&2; exit 1; }
step() { printf '\n\033[1m== %s ==\033[0m\n' "$1"; }

require() {
  command -v "$1" >/dev/null 2>&1 || fail "$1 not on PATH"
}

require curl
require jq

# ---------------------------------------------------------------
step "1. Waiting for app at $BASE_URL"
deadline=$(( $(date +%s) + TIMEOUT ))
while :; do
  if $CURL -o /dev/null -w '%{http_code}' "$BASE_URL/" 2>/dev/null | grep -qE '^(2|3|4)'; then
    pass "app responding"
    break
  fi
  if [ "$(date +%s)" -ge "$deadline" ]; then
    fail "app didn't respond within ${TIMEOUT}s"
  fi
  sleep 2
done

# ---------------------------------------------------------------
step "2. POST /api/competitors  (Arrive, isOwn=true)"
arrive_id=$(
  $CURL -X POST "$BASE_URL/api/competitors" \
    -H 'content-type: application/json' \
    -d '{"name":"Arrive","domain":"arrive.com","isOwn":true}' \
  | jq -r '.id // empty'
)
[ -n "$arrive_id" ] || fail "POST /api/competitors returned no id"
pass "created Arrive id=$arrive_id"

step "3. POST /api/competitors  (competitor, isOwn=false)"
comp_id=$(
  $CURL -X POST "$BASE_URL/api/competitors" \
    -H 'content-type: application/json' \
    -d '{"name":"Zillow","domain":"zillow.com","isOwn":false}' \
  | jq -r '.id // empty'
)
[ -n "$comp_id" ] || fail "POST /api/competitors returned no id"
pass "created Zillow id=$comp_id"

# ---------------------------------------------------------------
step "4. POST /api/competitors/$comp_id/refresh"
$CURL -X POST "$BASE_URL/api/competitors/$comp_id/refresh" -o /dev/null -w '%{http_code}\n' \
  | grep -qE '^2' || fail "refresh did not return 2xx"
pass "refresh accepted"

# ---------------------------------------------------------------
step "5. GET /api/benchmark"
benchmark=$($CURL "$BASE_URL/api/benchmark")
echo "$benchmark" | jq -e '.own.competitor.domain == "arrive.com"' >/dev/null \
  || fail "benchmark.own is not Arrive"
echo "$benchmark" | jq -e '.competitors | length >= 1' >/dev/null \
  || fail "benchmark.competitors is empty"
pass "benchmark has Arrive in 'own' and >= 1 competitor"

# ---------------------------------------------------------------
step "6. GET /api/competitors/$comp_id/keyword-gap?type=MISSING"
gap=$($CURL "$BASE_URL/api/competitors/$comp_id/keyword-gap?type=MISSING")
echo "$gap" | jq -e 'type == "array"' >/dev/null \
  || fail "keyword-gap did not return a JSON array"
pass "keyword-gap returned $(echo "$gap" | jq 'length') rows"

# ---------------------------------------------------------------
step "7. Cleanup"
$CURL -X DELETE "$BASE_URL/api/competitors/$comp_id" -o /dev/null
$CURL -X DELETE "$BASE_URL/api/competitors/$arrive_id" -o /dev/null
pass "deleted test competitors"

printf '\n\033[32mSMOKE OK\033[0m  %s\n' "$BASE_URL"
