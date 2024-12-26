/** AUTHORIZED role means the person can log in, but are not otherwise any of the other roles */
insert into wss_user_role(name) values ('AUTHORIZED');
/** Data admin has 'edit-all' permissions. */
insert into wss_user_role(name) values ('DATA_ADMIN');
insert into wss_user_role(name) values ('DISPATCHER');
insert into wss_user_role(name) values ('DRIVER');
insert into wss_user_role(name) values ('SITE_MANAGER');
alter table wss_user add column removed boolean not null default false;
