alter table driver drop column opted_out;
alter table driver add column black_listed boolean not null default false;
alter table driver add column availability varchar(1024);
alter table driver add column comments varchar(1024);
alter table driver add constraint driver_phone_uk unique(phone);
