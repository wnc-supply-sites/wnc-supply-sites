alter table site add column contact_number varchar(32);
create unique index item_name_unique on item (LOWER(name));
