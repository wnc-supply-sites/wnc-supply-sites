CREATE TYPE volunteer_delivery_status_enum AS ENUM ('PENDING', 'ACCEPTED', 'COMPLETED', 'CANCELLED');

CREATE TABLE volunteer_delivery (
  id SERIAL PRIMARY KEY,
  volunteer_name varchar(256) NOT NULL,
  volunteer_phone varchar(24) NOT NULL CHECK (volunteer_phone ~ '^[0-9]{10}$'),
  site_id integer NOT NULL REFERENCES site(id),
  status volunteer_delivery_status_enum NOT NULL DEFAULT 'PENDING',
  last_updated timestamptz NOT NULL DEFAULT now(),
  date_created timestamptz NOT NULL DEFAULT now(),
  url_key varchar(16) UNIQUE NOT NULL
);

CREATE TABLE volunteer_delivery_item (
  id SERIAL PRIMARY KEY,
  site_item_id INT NOT NULL REFERENCES site_item(id),
  volunteer_delivery_id INT NOT NULL REFERENCES volunteer_delivery(id)
);

CREATE INDEX idx_volunteer_delivery_site_id ON volunteer_delivery(site_id);
CREATE INDEX idx_volunteer_delivery_item_id ON volunteer_delivery_item(site_item_id);
CREATE INDEX idx_volunteer_delivery_volunteer_id ON volunteer_delivery_item(volunteer_delivery_id);


ALTER TABLE volunteer_delivery owner TO wnc_helene;
ALTER TABLE volunteer_delivery_item owner TO wnc_helene;