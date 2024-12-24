alter table sms_passcode add column validation_token_used boolean not null default false;
alter table wss_user_pass_change_history alter column date_changed set default now();
alter table wss_user add column last_updated timestamptz not null default now();
alter table wss_user_pass_change_history owner to wnc_helene;
