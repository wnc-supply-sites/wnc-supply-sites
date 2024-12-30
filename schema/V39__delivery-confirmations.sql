create table delivery_confirmation (
  id serial primary key,
  delivery_id integer not null references delivery(id),
  confirm_type varchar(16) not null, -- DISPATCHER|DRIVER|PICKUP|DROPOFF
  delivery_accepted boolean, -- null means no decision yet; false means not accepted, true means accepted.
  secret_code varchar(6) not null, -- secret code value that allows for the confirmation button to be visible on delivery manifest page
  date_created timestamptz not null default now(),
  date_confirmed timestamptz
);
alter table delivery_confirmation owner to wnc_helene;

alter table delivery_confirmation
    add constraint delivery_conf_type
      check (confirm_type in ('DRIVER', 'PICKUP_SITE', 'DROPOFF_SITE'));

alter table delivery add column dispatch_code varchar(8);
update delivery set dispatch_code = '0000';
alter table delivery alter column dispatch_code set not null;


update site set contact_number = null where contact_number = '';

alter table delivery add column driver_status varchar(32) not null default 'PENDING';
alter table delivery add constraint delivery_route_status
  check (driver_status in ('PENDING', 'DRIVER_EN_ROUTE', 'ARRIVED_AT_PICKUP', 'DEPARTED_PICKUP', 'ARRIVED_AT_DROP_OFF'));


/*
  Secret code value that can be used to change driver status
  When drivers update their driver status, this code is a URL parameter to authorize the request.

  Do not confuse this with the confirmation code given to a driver. The confirmation code
  can be used to confirm, while the driver code can be used to change driver status.
*/
alter table delivery add column driver_code varchar(16);
update delivery set driver_code = '1111';
alter table delivery alter column driver_code set not null;
