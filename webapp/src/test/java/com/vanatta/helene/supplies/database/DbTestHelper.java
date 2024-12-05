package com.vanatta.helene.supplies.database;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;

import jakarta.annotation.Nullable;
import java.util.List;

/** Various helper methods/queries for DB queries, for use in tests. */
public class DbTestHelper {

  public static class DispatchRequest {
    public static int countItemsInDispatchRequest(String publicId) {
      String query =
          """
        select count(*) from dispatch_request dr
        join dispatch_request_item dri on dri.dispatch_request_id = dr.id
        where dr.public_id = :publicId
      """;
      return jdbiTest.withHandle(
          handle ->
              handle.createQuery(query).bind("publicId", publicId).mapTo(Integer.class).one());
    }

    @Nullable
    public static Long getDispatchAirtableIdByPublicId(String publicId) {
      String query =
          """
        select dr.airtable_id from dispatch_request dr
        where dr.public_id = :publicId
      """;
      return jdbiTest.withHandle(
          handle -> handle.createQuery(query).bind("publicId", publicId).mapTo(Long.class).findOne().orElse(null));
    }

    @Nullable
    public static String getDispatchPublicIdByAirtableId(long airtableId) {
      String query =
          """
                select dr.public_id from dispatch_request dr
                where dr.airtable_id = :airtable_id
              """;
      return jdbiTest.withHandle(
          handle ->
              handle.createQuery(query).bind("airtable_id", airtableId).mapTo(String.class).findOne().orElse(null));
    }

    public static Long getDispatchIdByPublicId(String publicId) {
      String query =
          """
        select dr.id from dispatch_request dr
        where dr.public_id = :publicId
      """;
      return jdbiTest.withHandle(
          handle -> handle.createQuery(query).bind("publicId", publicId).mapTo(Long.class).one());
    }

    @Nullable
    public static String getDispatchStatus(String publicId) {
      String query =
          """
        select dr.status from dispatch_request dr
        where dr.public_id = :publicId
      """;
      return jdbiTest.withHandle(
          handle ->
              handle
                  .createQuery(query)
                  .bind("publicId", publicId)
                  .mapTo(String.class)
                  .findOne()
                  .orElse(null));
    }

    public static List<Long> getDispatchRequestsBySite(String siteName) {
      String query =
          """
        select dr.id from dispatch_request dr
        where dr.site_id = (select id from site where name = :siteName)
      """;
      return jdbiTest.withHandle(
          handle ->
              handle
                  .createQuery(query) //
                  .bind("siteName", siteName)
                  .mapTo(Long.class)
                  .list());
    }
  }
}
