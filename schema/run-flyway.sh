#!/bin/bash

docker build . --tag local/flyway:latest

docker run \
  -e FLYWAY_URL=jdbc:postgresql://host.docker.internal:5432/wnc_helene_test \
  -e FLYWAY_USER=postgres \
  -e FLWAY_PASSWORD=blackbox \
  local/flyway:latest \
  -locations=filesystem:/flyway/sql \
  -password=blackbox \
  -validateMigrationNaming=true \
  -connectRetries=2 \
  repair migrate
  
  # -skipExecutingMigrations=true \

