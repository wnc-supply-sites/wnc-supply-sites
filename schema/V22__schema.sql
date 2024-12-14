create table delivery(
  id serial primary key,
  from_site_id integer not null references site(id),
  to_site_id integer not null references site(id),
  delivery_status varchar(32),
  target_delivery_date date,
  dispatcher_name varchar(64),
  dispatcher_number varchar(24),
  driver_name varchar(64),
  driver_number varchar(24),
  driver_license_plates varchar(128),
  last_updated timestamptz not null default now(),
  date_created timestamptz not null default now()
);
alter table delivery owner to wnc_helene;

create table delivery_item(
  id serial primary key,
  delivery_id integer not null references delivery(id),
  item_id integer not null references item(id),
  last_updated timestamptz not null default now(),
  date_created timestamptz not null default now()
);
alter table delivery_item owner to wnc_helene;
alter table delivery_item add constraint delivery_item_uk unique(delivery_id, item_id);
