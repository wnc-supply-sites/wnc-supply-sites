drop table driver_vehicle;
alter table driver drop column comments;
alter table driver drop column city;
alter table driver drop column state;

alter table driver add column location varchar(128);
