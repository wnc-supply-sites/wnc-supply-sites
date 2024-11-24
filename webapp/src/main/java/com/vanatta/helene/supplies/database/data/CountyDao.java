package com.vanatta.helene.supplies.database.data;

import java.util.List;
import org.jdbi.v3.core.Jdbi;

public class CountyDao {

  public static List<String> fetchFullCountyList(Jdbi jdbi) {
    String query = "select name from county order by name";
    return jdbi.withHandle(handle -> handle.createQuery(query).mapTo(String.class).list());
  }

  public static List<String> fetchActiveCountyList(Jdbi jdbi) {
    String query =
        """
        select c.name from county c
        where exists (select 1 from site where county_id = c.id)
        order by c.name;
        """;
    return jdbi.withHandle(handle -> handle.createQuery(query).mapTo(String.class).list());
  }
}
