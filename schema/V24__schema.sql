alter table delivery add column airtable_id integer not null unique;
alter table delivery add column dispatcher_notes varchar(2048);
