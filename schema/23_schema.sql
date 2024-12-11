-- column records phone numbers which were 'bad', phone
-- numbers that we should not try.
alter table site add column bad_numbers varchar(1024);
-- column to record notes on why a site is no longer active
alter table site add column inactive_reason varchar(1024);

-- indicates if the site is actively onboarded, has correct contact information entered, and is
-- managing their supplies (or has appointed someone to do so).
alter table site add column onboarded boolean not null default false;
