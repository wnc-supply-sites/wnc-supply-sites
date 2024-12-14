create sequence dispatch_request_number_seq;
alter sequence dispatch_request_number_seq owner to wnc_helene;

create table dispatch_request(
  id serial primary key,
  public_id varchar(256) not null unique,
  request_number integer not null unique,
  status varchar(32),
  priority varchar(32),
  item_id integer references item(id) not null,
  site_id integer references site(id) not null,
  date_created timestamptz not null default now(),
  last_updated timestamptz not null default now(),
  date_closed timestamptz
);
alter table dispatch_request owner to wnc_helene;

-- when date_closed is not null, then the request is closed
-- status: will be pending/cancelled/completed
-- priority: will be one of 'Urgently Needed' or 'Needed'


create table dispatch_send_queue(
  id serial primary key,
  dispatch_request_id integer references dispatch_request(id),
  send_success_date timestamptz,
  send_type varchar(16),
  date_created  timestamptz not null default now()
);

alter table dispatch_send_queue owner to wnc_helene;


-- SEND_TYPE should be in "NEW|CANCEL|UPDATE"
