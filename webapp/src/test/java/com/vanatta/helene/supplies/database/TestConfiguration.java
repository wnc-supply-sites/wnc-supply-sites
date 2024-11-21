package com.vanatta.helene.supplies.database;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.util.List;

public class TestConfiguration {
  public static final Jdbi jdbiTest =
      Jdbi.create("jdbc:postgresql://localhost:5432/wnc_helene_test", "wnc_helene", "wnc_helene")
          .installPlugin(new SqlObjectPlugin());

  public static void setupDatabase() {
    List.of(
            "delete from site_item",
            "delete from item",
            "delete from site",
            // site1, in Watauga county
            """
                insert into site(name, address, city, county_id, state, website, site_type_id) values(
                   'site1', 'address1', 'city1', (select id from county where name = 'Watauga'), 'NC', 'site1website',
                   (select id from site_type where name = 'Distribution Center')
                );
                """,
            // site2, in Buncombe county, not accepting donations
            """
                insert into site(name, address, city, county_id, state, accepting_donations, site_type_id) values(
                   'site2', 'address2', 'city2', (select id from county where name = 'Buncombe'), 'NC', false,
                   (select id from site_type where name = 'Distribution Center')
                );
                """,
            // site3, in Buncombe county, not active
            """
                insert into site(name, address, city, county_id, state, active, site_type_id) values(
                   'site3', 'address3', 'city2', (select id from county where name = 'Buncombe'), 'NC', false,
                   (select id from site_type where name = 'Distribution Center')
                );
                """,
            // site4, in Buncombe county, no items (but active), supply hub
            """
                insert into site(name, address, city, county_id, state, site_type_id) values(
                   'site4', 'address3', 'city2', (select id from county where name = 'Buncombe'), 'NC',
                   (select id from site_type where name = 'Supply Hub')
                );
                """,
            // site5, (no items & not active), name, address & details may be modified by tests, data will not be stable.
            """
                insert into site(name, address, city, county_id, state, site_type_id) values(
                   'site5', 'address5', 'city5', (select id from county where name = 'Buncombe'), 'NC',
                   (select id from site_type where name = 'Distribution Center')
                );
                """,
            "insert into item(name) values('water')",
            "insert into item(name) values('gloves')",
            "insert into item(name) values('used clothes')",
            "insert into item(name) values('new clothes')",
            "insert into item(name) values('random stuff')",
            """
               insert into site_item(site_id, item_id, item_status_id) values(
                (select id from site where name = 'site1'),
                (select id from item where name = 'water'),
                (select id from item_status where name = 'Available')
               )
            """,
            """
               insert into site_item(site_id, item_id, item_status_id) values(
                (select id from site where name = 'site1'),
                (select id from item where name = 'new clothes'),
                (select id from item_status where name = 'Urgent Need')
               )
            """,
            """
               insert into site_item(site_id, item_id, item_status_id) values(
                (select id from site where name = 'site1'),
                (select id from item where name = 'used clothes'),
                (select id from item_status where name = 'Oversupply')
               )
            """,
            """
               insert into site_item(site_id, item_id, item_status_id) values(
                (select id from site where name = 'site2'),
                (select id from item where name = 'used clothes'),
                (select id from item_status where name = 'Oversupply')
               )
            """,
            """
               insert into site_item(site_id, item_id, item_status_id) values(
                (select id from site where name = 'site2'),
                (select id from item where name = 'water'),
                (select id from item_status where name = 'Oversupply')
               )
            """,
            // insert item for inactive 'site3'
            """
               insert into site_item(site_id, item_id, item_status_id) values(
                (select id from site where name = 'site3'),
                (select id from item where name = 'water'),
                (select id from item_status where name = 'Need')
               )
            """)
        .forEach(sql -> jdbiTest.withHandle(handle -> handle.createUpdate(sql).execute()));
  }
}
