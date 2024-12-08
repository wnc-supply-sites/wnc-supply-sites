alter table site_item
  add column wss_id integer not null unique default nextval('wss_id');
alter table site_item
  add column created_date timestamptz not null default now();
