alter table site_item
  add column last_updated timestamptz not null default '2024-11-01 12:00';

alter table site
  add column last_updated timestamptz not null default '2024-11-01 12:00';

update site set name = 'We The People Mission - Avery County Airport'
  where name = '"We The People" Mission - Avery County Airport';
