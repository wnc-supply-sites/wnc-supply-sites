/** add additional data fields to site table */
alter table site add column publicly_visible boolean not null default true;
alter table site add column contact_email varchar(256);
alter table site add column distributing_supplies boolean not null default true;

/** Add wss_id column */

-- create a single sequence for generating all public facing (WSS) IDs
create sequence wss_id;
alter sequence wss_id owner to wnc_helene;


-- use 'wss_id' as a public facing ID value for
-- data integrations. This allows us to set up test
-- data more easily and keeps the external facing ID cleaner
alter table site add column wss_id integer not null unique
  default nextval('wss_id');
alter table item add column wss_id integer not null unique
  default nextval('wss_id');

/**  move 'state' to be a column of county table */
alter table county add column state varchar(2) not null default 'NC';
alter table site drop column state;

create unique index county_unique  on county(name, state);


