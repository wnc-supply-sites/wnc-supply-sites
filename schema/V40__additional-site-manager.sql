create table additional_site_manager (
  id serial primary key,
  site_id integer not null references site(id),
  name varchar(128) not null,
  phone varchar(16) not null
);
alter table additional_site_manager owner to wnc_helene;
alter table additional_site_manager
  add constraint additional_site_manager_uk unique (site_id, phone);
