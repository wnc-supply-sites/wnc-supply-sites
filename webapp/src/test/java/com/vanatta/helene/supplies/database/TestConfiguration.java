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
                insert into site(name, address, city, county_id, state) values(
                   'site1', 'address1', 'city1', (select id from county where name = 'Watauga'), 'NC'
                );
                """,
            // site2, in Buncombe county, not accepting donations
            """
                insert into site(name, address, city, county_id, state, accepting_donations) values(
                   'site2', 'address2', 'city2', (select id from county where name = 'Buncombe'), 'NC', false
                );
                """,
            // site3, in Buncombe county, not active
            """
                insert into site(name, address, city, county_id, state, active) values(
                   'site3', 'address3', 'city2', (select id from county where name = 'Buncombe'), 'NC', false
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
                (select id from item_status where name = 'Requested')
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
                (select id from item_status where name = 'Requested')
               )
            """)
        .forEach(sql -> jdbiTest.withHandle(handle -> handle.createUpdate(sql).execute()));
  }
}
