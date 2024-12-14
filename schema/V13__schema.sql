alter table site add column airtable_id integer unique;
alter table dispatch_request add column airtable_id integer unique;
alter table item add column airtable_id integer unique;

alter table site add column public_visibility boolean not null default true;
alter table site add column hours varchar(512);
alter table site add column contact_name varchar(128);
alter table site add column email varchar(128);
alter table site add column facebook varchar(256);


alter table item add column last_updated timestamptz not null default now();
