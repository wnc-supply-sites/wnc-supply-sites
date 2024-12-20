create table site_distance_matrix(
  id serial primary key ,
  site1_id integer not null references site(id),
  site2_id integer not null references site(id),
  distance integer,
  drive_time integer
);

alter table site_distance_matrix owner to wnc_helene;

alter table site_distance_matrix
  add constraint site_distance_matrix_uk unique (site1_id, site2_id);

alter table site_distance_matrix
  add constraint site_distance_matrix_calculated check
     ((distance is null and drive_time is null) or (distance is not null and drive_time is not null));
