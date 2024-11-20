package com.vanatta.helene.supplies.database.filters;

import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class FilterDataDao {
  public static List<String> getAllCounties(Jdbi jdbi) {
    String query = "select name from county order by name";
    return jdbi.withHandle(handle -> handle.createQuery(query).mapTo(String.class).list());
  }

  public static List<String> getAllItems(Jdbi jdbi) {
    String query =
        """
          select name from item order by lower(name)
        """;
    return jdbi.withHandle(handle -> handle.createQuery(query).mapTo(String.class).list());
  }

  public static List<String> getAllActiveSitesThatHaveItems(Jdbi jdbi) {
    String query =
        """
        select site.name
        from site
        where site.active = true 
          and exists (select site_id from site_item where site_id = site.id)
        order by lower(site.name)
        """;
    return jdbi.withHandle(handle -> handle.createQuery(query).mapTo(String.class).list());
  }
}
