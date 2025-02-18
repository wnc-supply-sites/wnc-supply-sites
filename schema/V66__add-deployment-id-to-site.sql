ALTER TABLE site ADD COLUMN deployment_id INT;

UPDATE site SET deployment_id = (SELECT deployment_states.deployment_id FROM deployment_states INNER JOIN county on county.state = deployment_states.state LIMIT 1);

ALTER TABLE site ADD CONSTRAINT site_deployment_id_fkey FOREIGN KEY (deployment_id) REFERENCES deployment(id);

ALTER TABLE site ALTER COLUMN deployment_id set NOT NULL;

