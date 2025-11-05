
-- as a quick work-around, we are going to list both countries and states
-- states are stored in the county table
-- in order to store country names in the 'county' table, we need
-- to make the 'state' name column wider
-- The choice of 16 currenty matches the 'deployment_state' table.
-- In the longer term, we should allow for longer country names (and
-- really should just model countries properly altogether and not
-- hacky storing countries in the 'county' table.
alter table county alter column state type varchar(16);


