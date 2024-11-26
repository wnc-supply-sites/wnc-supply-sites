package com.vanatta.helene.supplies.database.filters;

import java.util.List;
import org.jdbi.v3.core.Jdbi;

public class FilterDataDao {
  public static List<String> getAllItems(Jdbi jdbi) {
    String query =
        """
          select name from item order by lower(name)
        """;
    return jdbi.withHandle(handle -> handle.createQuery(query).mapTo(String.class).list());
  }

  public static List<String> getAllActiveSites(Jdbi jdbi) {
    String query =
        """
        select site.name
        from site
        where site.active = true
        order by lower(site.name)
        """;
    return jdbi.withHandle(handle -> handle.createQuery(query).mapTo(String.class).list());
  }
}
