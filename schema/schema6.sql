create table site_type (
  id serial primary key,
  name varchar(32) not null unique
);
alter table site_type owner to wnc_helene;

insert into site_type(name) values ('Distribution Center');
insert into site_type(name) values ('Supply Hub');

alter table site add column site_type_id
  integer references site_type (id);

update site set site_type_id = (select id from site_type where name = 'Distribution Center');
alter table site alter column site_type_id set not null;

update site set site_type_id = (select id from site_type where name = 'Supply Hub')
  where name in ('Jimmy & Jeans',  'Resilient Recovery NC');

