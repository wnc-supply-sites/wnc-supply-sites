-- CA is missing from deployment_states for 'wnc-supply-sites.com'
insert into deployment_states(deployment_id, state)
values ((select id from deployment where short_name = 'WNC'),
        'CA');