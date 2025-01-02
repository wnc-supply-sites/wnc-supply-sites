alter table item add column date_created timestamptz not null default now();
