#!/bin/bash

set -eux

docker run \
  --rm \
  -e DB_URL=host.docker.internal:5432 
  $@ helene-supplies-webapp

