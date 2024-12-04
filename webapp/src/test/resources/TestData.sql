delete from dispatch_request_item;
delete from dispatch_request;
delete from site_item;
delete from item;
delete from site;

-- site1, in Watauga county            
insert into site(name, address, city, county_id, website, site_type_id) values(
'site1', 'address1', 'city1', (select id from county where name = 'Watauga'), 'site1website',
(select id from site_type where name = 'Distribution Center')
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

insert into site_item(site_id, item_id, item_status_id) values(
    (select id from site where name = 'site1'),
    (select id from item where name = 'water'),
    (select id from item_status where name = 'Available')
   );
insert into site_item(site_id, item_id, item_status_id) values(
    (select id from site where name = 'site1'),
    (select id from item where name = 'new clothes'),
    (select id from item_status where name = 'Urgently Needed')
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

insert into county(name) values('dummy') on conflict do nothing;

insert into dispatch_request(id, public_id, site_id)
values( -1, '#1', (select id from site where name = 'site6'));

insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
values(
  -1,
  (select id from item where name = 'water'),
  (select id from item_status where name = 'Needed')
);

insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
values(
  -1,
  (select id from item where name = 'used clothes'),
  (select id from item_status where name = 'Needed')
);

insert into dispatch_request_item(dispatch_request_id, item_id, item_status_id)
values(
  -1,
  (select id from item where name = 'gloves'),
  (select id from item_status where name = 'Urgently Needed')
);
