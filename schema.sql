-- wnc_helene   DDL


create table county
(
    id   serial primary key,
    name varchar(256) unique not null
);

create table county_adjacency
(
    id                 serial primary key,
    county_id          integer references county (id) not null,
    adjacent_county_id integer references county (id) not null
);

alter table county_adjacency
    add constraint count_adjacency_uk unique (county_id, adjacent_county_id);


create table site
(
    id                  serial primary key,
    name                varchar(256)                   not null unique,
    address             varchar(512)                   not null,
    city                varchar(256)                   not null,
    county_id           integer references county (id) not null,
    state               varchar(2)                     not null,
    accepting_donations boolean default true           not null,
    active              boolean default true           not null
);


create table item
(
    id               serial primary key,
    name             varchar(128) not null unique
);

create table site_need
(
    id      serial primary key,
    site_id integer references site (id) not null,
    item_id integer references item (id) not null
);

alter table site_need
    add constraint site_need_uk unique (site_id, item_id);

create table site_supply
(
    id      serial primary key,
    site_id integer references site (id) not null,
    item_id integer references item (id) not null
);


alter table site_supply
    add constraint site_supply_uk unique (site_id, item_id);

-------

insert into county (name) values ('Ashe');
insert into county (name) values ('Avery');
insert into county (name) values ('Buncombe');
insert into county (name) values ('Burke');
insert into county (name) values ('Caldwell');
insert into county (name) values ('Catawba');
insert into county (name) values ('Cocke');
insert into county (name) values ('Hamblen');
insert into county (name) values ('Hamblen county (name) values ');
insert into county (name) values ('Hancock');
insert into county (name) values ('Haywood');
insert into county (name) values ('Henderson');
insert into county (name) values ('Iredell');
insert into county (name) values ('Jackson');
insert into county (name) values ('Jefferson');
insert into county (name) values ('Knox');
insert into county (name) values ('Macon');
insert into county (name) values ('Madison');
insert into county (name) values ('McDowell');
insert into county (name) values ('Mecklenburg');
insert into county (name) values ('Mitchell');
insert into county (name) values ('Other');
insert into county (name) values ('Rutherford');
insert into county (name) values ('Sevier');
insert into county (name) values ('Sullivan');
insert into county (name) values ('Swain');
insert into county (name) values ('Washington');
insert into county (name) values ('Watauga');
insert into county (name) values ('Wilkes');
insert into county (name) values ('Yancey');

