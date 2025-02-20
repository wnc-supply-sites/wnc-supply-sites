INSERT INTO deployment_states(deployment_id, state)
VALUES(
   (SELECT id FROM deployment WHERE short_name = 'staging'),
     'KY'
);

INSERT INTO deployment_states(deployment_id, state)
VALUES(
   (SELECT id FROM deployment WHERE short_name = 'staging'),
     'IN'
);

INSERT INTO deployment_states(deployment_id, state)
VALUES(
   (SELECT id FROM deployment WHERE short_name = 'staging'),
     'TN'
);

INSERT INTO deployment_states(deployment_id, state)
VALUES(
   (SELECT id FROM deployment WHERE short_name = 'staging'),
     'WV'
);

INSERT INTO deployment_states(deployment_id, state)
VALUES(
   (SELECT id FROM deployment WHERE short_name = 'staging'),
     'VA'
);