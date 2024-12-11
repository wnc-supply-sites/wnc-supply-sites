drop table dispatch_send_queue;
drop table dispatch_request;

create table dispatch_request(
  id serial primary key,
  public_id varchar(256) not null unique,
  status varchar(32) not null default 'NEW',
  priority varchar(32) not null default 'Needed',
  site_id integer references site(id) not null,
  date_created timestamptz not null default now(),
  last_updated timestamptz not null default now(),
  date_closed timestamptz
);
alter table dispatch_request owner to wnc_helene;

-- when date_closed is not null, then the request is closed
-- status: will be new/pending/cancelled/completed
-- priority: value should be one of 'Needed' or 'Urgently Needed'


create table dispatch_request_item(
  id serial primary key,
  dispatch_request_id integer references dispatch_request(id) not null,
  item_id integer references item(id) not null,
  item_status_id integer references item_status(id) not null,
  date_created timestamptz not null default now()
);
alter table dispatch_request_item owner to wnc_helene;
create unique index dri_unique
  on dispatch_request_item(dispatch_request_id, item_id);

