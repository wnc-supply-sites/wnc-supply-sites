drop table driver_auth;
drop table driver_dates;

alter table driver add column active boolean not null default true;
alter table driver add column opted_out boolean not null default false;
alter table driver drop column email;
alter table driver add column license_plates varchar(256);
alter table driver drop column default_start_time;
alter table driver drop column default_end_time;

alter table driver owner to wnc_helene;
