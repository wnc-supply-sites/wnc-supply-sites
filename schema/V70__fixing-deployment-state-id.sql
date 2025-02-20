--Version 69 used lowercase 's' for short_name
UPDATE deployment_states
set deployment_id = (SELECT id FROM deployment WHERE short_name = 'Staging')
Where deployment_id IS NULL;