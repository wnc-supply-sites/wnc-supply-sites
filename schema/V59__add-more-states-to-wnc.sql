insert into deployment_states(deployment_id, state)
values(
   (select id from deployment where short_name = 'WNC'),
     'WV'
);

insert into deployment_states(deployment_id, state)
values(
   (select id from deployment where short_name = 'WNC'),
     'IN'
);

insert into deployment_states(deployment_id, state)
values(
   (select id from deployment where short_name = 'WNC'),
     'KY'
);