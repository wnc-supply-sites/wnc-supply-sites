create table item_tag(
  id serial primary key,
  item_id integer references item(id) not null,
  tag_name varchar(64) not null,
  date_created timestamptz not null default now()
);
alter table item_tag owner to wnc_helene;

alter table item_tag add constraint
  item_category_uk unique(item_id, tag_name);
