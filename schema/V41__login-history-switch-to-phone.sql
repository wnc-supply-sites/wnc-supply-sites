delete from login_history;
alter table login_history drop column ip;
alter table login_history add column phone_number varchar(16) not null;
