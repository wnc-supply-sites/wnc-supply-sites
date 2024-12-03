package com.vanatta.helene.supplies.database;

import com.vanatta.helene.supplies.database.test.util.TestDataFile;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public class TestConfiguration {
  public static final Jdbi jdbiTest =
      Jdbi.create("jdbc:postgresql://localhost:5432/wnc_helene_test", "wnc_helene", "wnc_helene")
          .installPlugin(new SqlObjectPlugin());

  // public IDs of the dispatches that we will creaete in the test data setup
  public static final String SITE1_NEW_DISPATCH = "#1 site1";
  public static final String SITE2_PENDING_DISPATCH = "#2 site2";
  public static final String SITE3_NEW_DISPATCH = "#30 site3 new";
  public static final String SITE3_PENDING_DISPATCH = "#33 site3 pending";
  public static final String SITE4_NO_DISPATCH = "#4 site4";

  public static void setupDatabase() {
    try {
      var sql = TestDataFile.TEST_DATA_SCHEMA.readData();
      TestConfiguration.jdbiTest.withHandle(handle -> handle.createScript(sql).execute());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static long getSiteId() {
    return getSiteId("site1");
  }

  public static long getSiteId(String siteName) {
    return TestConfiguration.jdbiTest.withHandle(
        handle ->
            handle
                .createQuery("select id from site where name = :siteName")
                .bind("siteName", siteName)
                .mapTo(Long.class)
                .one());
  }

  /** Sets up additional data in DB for dispatch requests. */
  public static void setupDispatchRequests() {
    //     DB setup
    // site1: has a NEW dispatch request
    // site2: has a PENDING dispatch request
    // site3: has a NEW & PENDING dispatch request
    // site4: no dispatch requests
    List.of(
            String.format(
                """
        insert into dispatch_request(public_id, status, site_id)
        values(
          '%s',
          'NEW',
          (select id from site where name = 'site1')
        )
        """,
                SITE1_NEW_DISPATCH),
            String.format(
                """
            insert into dispatch_request(public_id, status, site_id)
            values(
              '%s',
              'PENDING',
              (select id from site where name = 'site2')
            )
            """,
                SITE2_PENDING_DISPATCH),
            String.format(
                """
            insert into dispatch_request(public_id, status, site_id)
            values(
              '%s',
              'NEW',
              (select id from site where name = 'site3')
            )
            """,
                SITE3_NEW_DISPATCH),
            String.format(
                """
                insert into dispatch_request(public_id, status, site_id)
                values(
                  '%s',
                  'PENDING',
                  (select id from site where name = 'site3')
                )
                """,
                SITE3_PENDING_DISPATCH))
        .forEach(sql -> jdbiTest.withHandle(handle -> handle.createUpdate(sql).execute()));
  }
}
