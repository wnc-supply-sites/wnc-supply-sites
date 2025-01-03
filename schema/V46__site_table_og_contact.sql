/**
  New column that represents the original creator of a site.
  This person never loses access to be able to edit the site.
  This prevents a user from updating a site to remove their own access.
  We could instead print some warning messages if a user removes
  them self from their own site. This DB column that can never be changed via
  the UI is an alternative (arguably hacky).
 */
alter table site add column og_contact_number varchar(16);
update site set og_contact_number = contact_number;
alter table site alter column og_contact_number set not null;
