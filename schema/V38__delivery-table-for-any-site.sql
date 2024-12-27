/** Update delivery table to keep full information, so that we can still do delivery tracking
  even if the sites are not registered in WSS (the website).
 */

alter table delivery add column pickupSiteName varchar(128);
alter table delivery add column pickupContactName varchar(128);
alter table delivery add column pickupContactPhone varchar(32);
alter table delivery add column pickupHours varchar(512);
alter table delivery add column pickupAddress varchar(256);
alter table delivery add column pickupCity varchar(128);
alter table delivery add column pickupState varchar(16);

alter table delivery add column dropoffSiteName varchar(128);
alter table delivery add column dropoffContactName varchar(128);
alter table delivery add column dropoffContactPhone varchar(32);
alter table delivery add column dropoffHours varchar(512);
alter table delivery add column dropoffAddress varchar(256);
alter table delivery add column dropoffCity varchar(128);
alter table delivery add column dropoffState varchar(16);

alter table delivery alter column from_site_id drop not null;
alter table delivery alter column to_site_id drop not null;

alter table delivery_item alter column item_id drop not null;
alter table delivery_item add column item_name varchar(128);
alter table delivery_item add constraint delivery_item_item_name
  check (item_id is not null or item_name is not null);

