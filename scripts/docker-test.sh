#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${REPO_ROOT}"

cleanup() {
  echo "[docker:test] Cleaning up compose stack"
  docker compose -f docker/docker-compose.yml down -v >/dev/null 2>&1 || true
}
trap cleanup EXIT

echo "[docker:test] Preparing build environment"
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$PWD/.gradle-local}"
echo "[docker:test] Stopping any existing containers"
docker compose -f docker/docker-compose.yml down -v >/dev/null 2>&1 || true
echo "[docker:test] Building project + Docker dependencies"
task docker:build
echo "[docker:test] Starting Fuseki container"
docker compose -f docker/docker-compose.yml up -d

echo "[docker:test] Waiting for Fuseki health endpoint"
ready=0
for _ in $(seq 1 60); do
  if curl -fsS 'http://localhost:3030/$/ping' >/dev/null 2>&1; then
    ready=1
    break
  fi
  sleep 1
done
[ "${ready}" -eq 1 ]
echo "[docker:test] Fuseki is healthy"

echo "[docker:test] Loading test data into /test/data"
curl --retry 5 --retry-delay 1 --retry-connrefused -fsS \
  -X PUT \
  -H 'Content-Type: text/turtle' \
  --data-binary @src/test/resources/test.ttl \
  'http://localhost:3030/test/data'

echo "[docker:test] Checking loaded graph size via schema:hasPart"
has_part_query='PREFIX schema: <https://schema.org/> SELECT (COUNT(*) AS ?count) WHERE { ?s schema:hasPart ?o }'
has_part_count="$(curl --retry 5 --retry-delay 1 --retry-connrefused -fsSG 'http://localhost:3030/test/query' \
  -H 'Accept: application/sparql-results+json' \
  --data-urlencode "query=${has_part_query}" \
  | jq -r '.results.bindings[0].count.value')"
echo "[docker:test] schema:hasPart triple count: ${has_part_count}"

echo "[docker:test] Running SPARQL verification query"
query="$(cat <<'SPARQL'
PREFIX cnf: <https://linked.data.gov.au/def/cn/func/>
SELECT ?partTypes ?partValuePredicate ?partValue
WHERE {
  BIND(<https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> AS ?iri)
  ?iri cnf:getParts (?partIds ?partTypes ?partValuePredicate ?partValue) .
}
SPARQL
)"

response="$(curl --retry 5 --retry-delay 1 --retry-connrefused -fsSG 'http://localhost:3030/test/query' \
  -H 'Accept: application/sparql-results+json' \
  --data-urlencode "query=${query}")"

echo "[docker:test] Asserting expected row count and key binding"
if ! echo "${response}" | jq -e '.results.bindings | length == 12' >/dev/null; then
  echo "[docker:test] Assertion failed: expected 12 bindings"
  echo "${response}" | jq .
  echo "${response}" | jq '.results.bindings | length'
  echo "${response}" | jq '.results.bindings[:5]'
  exit 1
fi
if ! echo "${response}" | jq -e '.results.bindings[] | select(.partTypes.value == "<https://linked.data.gov.au/def/addr-part-types/buildingLevelNumber>" and .partValuePredicate.value == "https://schema.org/value" and .partValue.value == "2")' >/dev/null; then
  echo "[docker:test] Assertion failed: expected buildingLevelNumber schema:value 2 binding"
  echo "${response}" | jq .
  exit 1
fi
echo "[docker:test] All assertions passed"
