delete from site_item_audit;
delete from site_item;
delete from item;
delete from site_audit_trail;
delete from site;
delete from county;

insert into county(name, state) values('Ashe', 'NC');
insert into county(name, state) values('Buncombe', 'NC');

insert into county(name, state) values('Watauga', 'NC');
insert into county(name, state) values('Sevier', 'TN');
insert into county(name, state) values('Halifax', 'VA');
insert into county(name) values('dummy') on conflict do nothing;

insert into site(
  name,
  contact_name,
  contact_number,
  contact_email,
  address,
  city,
  county_id,
  website,
  site_type_id,
  accepting_donations,
  active,
  airtable_id,
  hours,
  facebook,
  wss_id,
  publicly_visible,
  distributing_supplies)
values (
        'site1',
        'contact me',
        '111',
        'email glorious',
        'address1',
        'city1',
        (select id from county where name = 'Watauga' and state = 'NC'),
        'site1website',
        (select id from site_type where name = 'Distribution Center'),
        true,
        true ,
        -200,
        'our hours',
        'fb url',
        -10,
        true,
        true
       );

-- site2, in Buncombe county, not accepting donations
insert into site(name, address, city, county_id, accepting_donations, site_type_id) values(
'site2', 'address2', 'city2', (select id from county where name = 'Buncombe'), false,
(select id from site_type where name = 'Distribution Center')
);

-- site3, in Buncombe county, not active
insert into site(name, address, city, county_id, active, site_type_id) values(
'site3', 'address3', 'city2', (select id from county where name = 'Buncombe'), false,
(select id from site_type where name = 'Distribution Center')
);

-- site4, in Buncombe county, no items (but active), supply hub
    insert into site(name, address, city, county_id, site_type_id) values(
       'site4', 'address3', 'city2', (select id from county where name = 'Buncombe'),
       (select id from site_type where name = 'Supply Hub')
    );

-- site5, (no items & not active), name, address & details may be modified by tests,
-- data will not be stable.
insert into site(name, address, city, county_id, site_type_id) values(
   'site5', 'address5', 'city5', (select id from county where name = 'Buncombe'),
   (select id from site_type where name = 'Distribution Center')
);

insert into site(name, address, city, county_id, website, site_type_id) values(
   'site6', 'address6', 'city6', (select id from county where name = 'Watauga'), 'site6website',
   (select id from site_type where name = 'Distribution Center')
);


insert into item(name) values('water');
insert into item(name) values('soap');
insert into item(name) values('gloves');
insert into item(name) values('used clothes');
insert into item(name) values('new clothes');
insert into item(name) values('random stuff');
insert into item(name) values('heater');
insert into item(name) values('batteries');

insert into site_item(site_id, item_id, item_status_id) values(
    (select id from site where name = 'site1'),
    (select id from item where name = 'water'),
    (select id from item_status where name = 'Available')
   );
insert into site_item(site_id, item_id, item_status_id, wss_id) values(
    (select id from site where name = 'site1'),
    (select id from item where name = 'new clothes'),
    (select id from item_status where name = 'Urgently Needed'),
    -10
   );
insert into site_item(site_id, item_id, item_status_id) values(
    (select id from site where name = 'site1'),
    (select id from item where name = 'used clothes'),
    (select id from item_status where name = 'Oversupply')
   );
 insert into site_item(site_id, item_id, item_status_id) values(
    (select id from site where name = 'site2'),
    (select id from item where name = 'used clothes'),
    (select id from item_status where name = 'Oversupply')
   );
insert into site_item(site_id, item_id, item_status_id) values(
    (select id from site where name = 'site2'),
    (select id from item where name = 'new clothes'),
    (select id from item_status where name = 'Oversupply')
  );
 insert into site_item(site_id, item_id, item_status_id) values(
    (select id from site where name = 'site2'),
    (select id from item where name = 'water'),
    (select id from item_status where name = 'Oversupply')
   );

-- insert item for inactive 'site3'
 insert into site_item(site_id, item_id, item_status_id) values(
    (select id from site where name = 'site3'),
    (select id from item where name = 'water'),
    (select id from item_status where name = 'Needed')
   );
-- insert a "dummy" county, where no site is in that county (this county is considered
-- 'not active')


