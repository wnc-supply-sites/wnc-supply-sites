create table deployment(
  id serial primary key,
  domain varchar(256) not null unique,
  short_name varchar(64) not null unique,
  contact_us_link varchar(256) not null,
  site_description varchar(128) not null
);
alter table deployment owner to wnc_helene;

create table deployment_states (
  id serial primary key,
  deployment_id integer references deployment(id),
  state varchar(16) not null
);
alter table deployment_states owner to wnc_helene;
alter table deployment_states add constraint deployment_states_uk unique(deployment_id, state);


insert into deployment(domain, short_name, contact_us_link, site_description)
values(
      'wnc-supply-sites.com',
   'WNC',
'https://form.jotform.com/243608573773062',
'Hurricane Helene Disaster Relief');

insert into deployment(domain, short_name, contact_us_link, site_description)
values(
    'socal-supply-sites.com',
 'SoCal',
'https://form.jotform.com/250106833918154',
 'LA Fires Disaster Relief');


insert into deployment_states(deployment_id, state)
values(
   (select id from deployment where short_name = 'WNC'),
     'NC'
);
insert into deployment_states(deployment_id, state)
values(
  (select id from deployment where short_name = 'WNC'),
    'TN'
);

insert into deployment_states(deployment_id, state)
values(
    (select id from deployment where short_name = 'SoCal'),
    'CA'
);

