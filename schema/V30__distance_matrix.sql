create table site_distance_matrix(
  id serial primary key ,
  site1_id integer not null references site(id),
  site2_id integer not null references site(id),
  distance_miles decimal(6,1),
  drive_time_seconds integer,
  valid boolean
);
/** valid => null if we don't know. False if the google maps API can't compute a distance. True if we can compute a distance */


alter table site_distance_matrix owner to wnc_helene;

alter table site_distance_matrix
  add constraint site_distance_matrix_uk unique (site1_id, site2_id);

alter table site_distance_matrix
  add constraint site_distance_matrix_calculated check
     ((distance_miles is null and drive_time_seconds is null) or (distance_miles is not null and drive_time_seconds is not null));

/**
  If valid is null or false, then distance & drive time should be too. If valid is false.
  Otherwise if valid is set, then we shoudl have distance and drive time computed
 */
alter table site_distance_matrix
  add constraint site_distance_valid check
    ((valid is null and distance_miles is null)
       or (
         valid = false and distance_miles is null) or (valid = true and distance_miles is not null));

alter table site_distance_matrix
  add constraint site_distance_distance_value check (distance_miles >= 0.0);
alter table site_distance_matrix
  add constraint site_distance_time_value check (drive_time_seconds >= 0.0);

insert into site_distance_matrix(site1_id, site2_id)
select s1.id, s2.id
from site s1
cross join site s2
where s1.id < s2.id;
