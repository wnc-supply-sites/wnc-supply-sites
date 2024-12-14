#!/bin/bash

set -eux

function runFlyway {
  local dbName="$1"
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
    repair migrate
#    -skipExecutingMigrations=true \
}

(
  cd "$(dirname "$0")"
  docker build . --tag local/flyway:latest
  runFlyway wnc_helene_test
  runFlyway wnc_helene
)
