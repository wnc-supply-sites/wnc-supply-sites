package com.vanatta.helene.supplies.database.data;

import com.vanatta.helene.supplies.database.supplies.filters.AuthenticatedMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdbi.v3.core.Jdbi;

public class CountyDao {

  public static List<String> fetchFullCountyListByState(Jdbi jdbi, String state) {
    String query = "select name from county where state = :state order by name";
    return jdbi.withHandle(
        handle -> handle.createQuery(query).bind("state", state).mapTo(String.class).list());
  }

  public static List<String> fetchActiveCountyList(Jdbi jdbi, AuthenticatedMode authenticatedMode) {
    String authenticatedFilter =
        authenticatedMode == AuthenticatedMode.AUTHENTICATED ? "" : " and publicly_visible = true";
    String query =
        String.format(
            """
        select
          c.name || ', ' || c.state
        from county c
        where exists (select 1 from site where county_id = c.id and active=true %s)
        order by c.state, c.name;
        """,
            authenticatedFilter);
    return jdbi.withHandle(handle -> handle.createQuery(query).mapTo(String.class).list());
  }

  /** Returns map of 'state' -> 'counties' */
  public static Map<String, List<String>> fetchFullCountyListing(Jdbi jdbi) {
    String query = "select name, state from county";
    List<Map<String, Object>> results =
        jdbi.withHandle(handle -> handle.createQuery(query).mapToMap().list());

    Map<String, List<String>> stateCounties = new HashMap<>();
    results.forEach(
        entry -> {
          List<String> counties =
              stateCounties.computeIfAbsent((String) entry.get("state"), _ -> new ArrayList<>());
          counties.add((String) entry.get("name"));
        });
    return stateCounties;
  }
}
