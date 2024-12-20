create table site_distance_matrix(
  id serial primary key ,
  site1 integer not null references site(id),
  site2 integer not null references site(id),
  distance integer,
  drive_time integer
);

alter table site_distance_matrix
  add constraint site_distance_matrix_uk unique (site1, site2);

alter table site_distance_matrix
  add constraint site_distance_matrix_calculated check
     ((distance is null and drive_time is null) or (distance is not null and drive_time is not null));
