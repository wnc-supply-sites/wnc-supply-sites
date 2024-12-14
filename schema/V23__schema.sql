-- column records phone numbers which were 'bad', phone
-- numbers that we should not try.
alter table site add column bad_numbers varchar(1024);
-- column to record notes on why a site is no longer active
alter table site add column inactive_reason varchar(1024);

-- indicates if the site is actively onboarded, has correct contact information entered, and is
-- managing their supplies (or has appointed someone to do so).
alter table site add column onboarded boolean not null default false;


create table max_supply_load(
  id serial primary key,
  sort_order integer unique not null,
  name varchar(64) unique not null
);
alter table max_supply_load owner to wnc_helene;

insert into max_supply_load(sort_order, name)
  values(10, 'Car');
insert into max_supply_load(sort_order, name)
  values(20, 'Pickup Truck');
insert into max_supply_load(sort_order, name)
  values(30, 'Box Truck');
insert into max_supply_load(sort_order, name)
  values(40, 'Semi Truck');

alter table site add column max_supply_load_id integer references max_supply_load(id);

alter table site add column has_forklift boolean not null default false;
alter table site add column has_loading_dock boolean not null default false;
alter table site add column has_indoor_storage boolean not null default false;
alter table site add column receiving_notes varchar(1024) not null default '';

alter table max_supply_load add column default_selection boolean;
update max_supply_load set default_selection = true where name = 'Pickup Truck';
update site set max_supply_load_id = (select id from max_supply_load where name = 'Pickup Truck');
alter table site alter column max_supply_load_id set not null;
-- if a site is created, it is most likely onboarded, the existing sites
-- are the ones that might not be properly onboarded.
alter table site alter column onboarded set default true;
