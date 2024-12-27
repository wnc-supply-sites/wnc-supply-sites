/** Update delivery table to keep full information, so that we can still do delivery tracking
  even if the sites are not registered in WSS (the website).
 */

alter table delivery add column pickup_site_name varchar(128);
alter table delivery add column pickup_contact_name varchar(128);
alter table delivery add column pickup_contact_phone varchar(32);
alter table delivery add column pickup_hours varchar(512);
alter table delivery add column pickup_address varchar(256);
alter table delivery add column pickup_city varchar(128);
alter table delivery add column pickup_state varchar(16);

alter table delivery add column dropoff_site_name varchar(128);
alter table delivery add column dropoff_contact_name varchar(128);
alter table delivery add column dropoff_contact_phone varchar(32);
alter table delivery add column dropoff_hours varchar(512);
alter table delivery add column dropoff_address varchar(256);
alter table delivery add column dropoff_city varchar(128);
alter table delivery add column dropoff_state varchar(16);

alter table delivery alter column from_site_id drop not null;
alter table delivery alter column to_site_id drop not null;

alter table delivery_item alter column item_id drop not null;
alter table delivery_item add column item_name varchar(128);
alter table delivery_item add constraint delivery_item_item_name
  check (item_id is not null or item_name is not null);

