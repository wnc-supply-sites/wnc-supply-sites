#!/bin/bash

set -eux

# Set skipTestFlag to 'skipTest' if migrations have already been applied to test

# EG: $0 skipTest

skipTestFlag="${1-}"


skipTest=false
if [ "$skipTestFlag" == "skipTest" ]; then
  skipTest=true
fi

function runFlyway {
  local dbName="$1"
  local skip="${2-false}"

  docker run \
    -e "FLYWAY_URL=jdbc:postgresql://host.docker.internal:5432/$dbName" \
    -e FLYWAY_USER=postgres \
    -e FLWAY_PASSWORD=blackbox \
    local/flyway:latest \
    -locations=filesystem:/flyway/sql \
    -password=blackbox \
    -validateMigrationNaming=true \
    -connectRetries=2 \
    -baselineOnMigrate=true \
    -skipExecutingMigrations="$skip" \
    repair migrate
}

(
  cd "$(dirname "$0")"
  docker build . --tag local/flyway:latest
  runFlyway wnc_helene_test "$skipTest"
  runFlyway wnc_helene
)
