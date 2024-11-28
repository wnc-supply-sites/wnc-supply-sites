drop table dispatch_send_queue;
drop table dispatch_request;

create table dispatch_request(
  id serial primary key,
  public_id varchar(256) not null unique,
  status varchar(32) not null default 'NEW',
  priority varchar(32) not null,
  site_id integer references site(id) not null,
  date_created timestamptz not null default now(),
  last_updated timestamptz not null default now(),
  date_closed timestamptz
);
alter table dispatch_request owner to wnc_helene;

-- when date_closed is not null, then the request is closed
-- status: will be new/pending/cancelled/completed
-- priority: value should match what we have in airTable for status


create dispatch_request_item(
  id serial primary key,
  dispatch_request_id integer reference dispatch_request(id) not null,
  item_id integer references item(id) not null,
  date_created timestamptz not null default now()
);
alter table dispatch_request_item owner to wnc_helene;


-- Table records 'send' requests to update a dispatch requests.
-- Send requests are routed to MAKE
create table dispatch_send_queue(
  id serial primary key,
  dispatch_request_id integer references dispatch_request(id),
  send_success_date timestamptz,
  send_type varchar(16),
  date_created timestamptz not null default now()
);
-- SEND_TYPE should be in "NEW|CANCEL|UPDATE"

alter table dispatch_send_queue owner to wnc_helene;
