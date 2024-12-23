alter table wss_user add column password_bcrypt varchar(64);

/** Keeps a record of when users change their passwords */
create table wss_user_pass_change_history(
  id serial primary key not null,
  wss_user_id integer references wss_user(id),
  date_changed timestamptz not null
);
