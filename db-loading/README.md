# Helene - CSV Loader

Places data from CSV file from 'supportingwnc.com' with
distribution sites & their wants into database.


# Loading DB

- install postgres

- Open psql, connect to DB and run:

```postgresql
CREATE USER wnc_helene WITH PASSWORD 'wnc_helene';
create database wnc_helene;
alter database wnc_helene owner to wnc_helene;

create database wnc_helene_test;
alter database wnc_helene_test owner to wnc_helene;
```


```bash
export PGPASSWORD=wnc_helene
psql -U wnc_helene -d wnc_helene -f schema.sql
psql -U wnc_helene -d wnc_helene_test -f schema.sql
```


(1) Load site-need data into database: Run 'site-need-loader' LoaderMain.java
(2) Load site-supply data into database: Run 'site-supply-loader' LoaderMain.java




