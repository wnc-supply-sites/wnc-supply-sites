-- quick cleanup, unused table
drop table vehicle_type;


/**
  Stores users, they are primarily identified by phone number
  The public_id value is a value we can store in a cookie to
  identify the user.
*/
create table wss_user(
  id serial primary key,
  public_id varchar(64) not null unique,
  phone varchar(16) not null unique,
  date_created timestamptz not null default now()
);

/**
  Table holds SMS passcodes, when a user enters their phone number
  we will be place an encrypted passcode into this table.

  Passcode is bcrypted.

  crf_bcrypted is a CRF token that we will pass to the front end.
  When confirming the passcode, we need the client to send us
  back the correct CRF token. If somehow someone sniffs out the SMS
  passcode value, they will not be able to use it without the CRF.

  Validation key is a temporary auth token that we give to a user
  after they have confirmed a given passcode. With that token
  they can then update their password. The validation token
  is bcrypted.
 */
create table sms_passcode(
  id serial primary key,
  wss_user_id integer not null references wss_user(id),
  passcode_bcrypted varchar(32) not null,
  confirmed boolean not null default false,
  crf_bcrypted varchar(64) not null,
  validation_key_bcrypted varchar(64) not null,
  date_created timestamptz not null default now(),
  date_confirmed timestamptz
);

/**
  After login, stores auth tokens that users will have in a cookie
  to confirm they are logged in.

  token is sha256 hashed
 */
create table wss_user_auth_key(
  id serial primary key,
  wss_user_id integer not null references wss_user(id),
  token_sha256 varchar(128) not null unique,
  date_created timestamptz not null default now(),
  valid_until timestamptz not null default now() + interval '60 days'
);

create table wss_user_sites(
  id serial primary key,
  wss_user_id integer not null references wss_user(id),
  site_id integer not null references site(id),
  date_created timestamptz not null default now()
);

alter table wss_user_sites add constraint wss_user_sites_uk unique(wss_user_id, site_id);


create table wss_user_role(
  id serial primary key,
  name varchar(32) not null unique
);

create table wss_user_roles(
  id serial primary key,
  wss_user_id integer not null references wss_user(id),
  wss_user_role_id integer not null references wss_user_role(id),
  date_created timestamptz not null default now()
);
alter table wss_user_roles add constraint wss_user_roles_uk unique(wss_user_id, wss_user_role_id);

