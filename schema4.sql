create table login_history (
  ip varchar(64) not null,
  result boolean not null,
  login_date timestamptz not null default now()
);
alter table login_history owner to wnc_helene;

create table auth_key (
  cookie_key varchar(64) not null
);
alter table auth_key owner to wnc_helene;

