alter table site add column additional_contacts varchar(1024);

-- table to keep track of changes to supply site info
-- eg: address, contact numbers
create table site_audit_trail (
  id serial primary key,
  site_id integer not null references site(id),
  field_name varchar(128),
  old_value varchar(512),
  new_value varchar(512),
  changed_date timestamptz not null default now()
);

alter table site_audit_trail owner to wnc_helene;
