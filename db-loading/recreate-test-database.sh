#!/bin/bash

#drop database wnc_helene
#create database wnc_helene;
#alter database wnc_helene owner to wnc_helene;
#
#create database wnc_helene_test;
#alter database wnc_helene_test owner to wnc_helene;

export PGPASSWORD=blackbox

set -eux

echo "drop database wnc_helene_test" | psql -U postgres
echo "create database wnc_helene_test" | psql -U postgres
echo "alter database wnc_helene_test owner to wnc_helene" | psql -U postgres

export PGPASSWORD=wnc_helene
psql -U wnc_helene -d wnc_helene_test -f schema.sql
