-- table that stores a secret value, a shared secret with incoming webhook requests.
-- if the incoming request does not have this secret value, we will reject it.
create table webhook_auth_secret (
  secret varchar(64) not null unique
);
alter table webhook_auth_secret owner to wnc_helene;
