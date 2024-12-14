-- records changes to an items status at a site
create table site_item_audit(
  id serial primary key,
  site_id integer not null references site(id),
  item_id integer not null references item(id),
  old_value varchar(512) not null,
  new_value varchar(512) not null,
  changed_date timestamptz not null default now()
);
alter table site_item_audit owner to wnc_helene;
