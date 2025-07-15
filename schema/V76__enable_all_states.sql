-- insert all states into 'deployment_states' for the wnc-supply-sites deployment

insert into deployment_states(deployment_id, state)
select deployment_id, state from
(
  select distinct
    (select id from deployment where domain = 'wnc-supply-sites.com') deployment_id,
    state as state
  from county
) A
-- Skip insert of any states that already exist
where not exists (select 1 from deployment_states where deployment_states.state = A.state)



