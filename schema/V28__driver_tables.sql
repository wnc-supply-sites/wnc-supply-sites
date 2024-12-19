create table driver(
  id serial primary key,
  airtable_id integer not null,
  name varchar(128) not null,
  email varchar(256) unique,
  phone varchar(32) not null,
  city varchar(64),
  state varchar(32),
  comments varchar(2048),
  last_updated timestamptz not null default now(),
  date_created timestamptz not null default now(),
  default_start_time time,
  default_end_time time
);

create table driver_dates(
  id serial primary key,
  driver_id integer not null references driver(id),
  available_date date not null,
  start_time time,
  end_time time
);
alter table driver_dates add constraint driver_date_unique unique (driver_id, available_date);

create table driver_auth(
  id serial primary key,
  driver_id integer not null unique references driver(id),
  auth_key varchar(64) not null unique,
  date_created timestamptz not null default now()
);

create table vehicle_type(
  id serial primary key,
  name varchar(32) not null unique
);

create table driver_vehicle(
  id serial primary key,
  driver_id integer not null references driver(id),
  license_plate varchar(16) not null,
  has_trailer boolean not null,
  vehicle_type_id integer not null references vehicle_type(id),
  date_created timestamptz not null default now(),
  last_updated timestamptz not null default now()
);

