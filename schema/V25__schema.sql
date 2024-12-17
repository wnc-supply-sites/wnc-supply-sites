delete from delivery_item;
delete from delivery;
alter table delivery add column public_url_key varchar(16) not null unique;
