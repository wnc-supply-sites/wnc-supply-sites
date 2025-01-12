package com.vanatta.helene.supplies.database.supplies.filters;

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

  public static List<String> getAllActiveSites(
      Jdbi jdbi, AuthenticatedMode authenticatedMode, List<String> stateList) {
    String authenticatedFilter =
        authenticatedMode == AuthenticatedMode.AUTHENTICATED ? "" : "and s.publicly_visible = true";

    String query =
        String.format(
            """
        select s.name
        from site s
        join county c on c.id = s.county_id
        where s.active = true
          %s
          and c.state in (<stateList>)
        order by lower(s.name)
        """,
            authenticatedFilter);

    return jdbi.withHandle(
        handle ->
            handle.createQuery(query).bindList("stateList", stateList).mapTo(String.class).list());
  }
}
