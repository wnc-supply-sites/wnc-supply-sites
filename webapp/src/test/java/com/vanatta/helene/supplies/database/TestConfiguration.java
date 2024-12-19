package com.vanatta.helene.supplies.database;

import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.data.SiteType;
import com.vanatta.helene.supplies.database.manage.add.site.AddSiteDao;
import com.vanatta.helene.supplies.database.manage.add.site.AddSiteData;
import com.vanatta.helene.supplies.database.test.util.TestDataFile;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

public class TestConfiguration {

  // these values come from TestData.sql
  public static final long SITE1_AIRTABLE_ID = -200;
  public static final long SITE1_WSS_ID = -10;
  public static final long SITE2_WSS_ID = -20;
  public static final long WATER_WSS_ID = -40;
  public static final long SOAP_WSS_ID = -30;
  public static final long GLOVES_WSS_ID = -50;
  public static final long USED_CLOTHES_WSS_ID = -60;
  public static final long NEW_CLOTHES_WSS_ID = -70;
  public static final long RANDOM_STUFF_WSS_ID = -80;
  public static final long HEATER_WSS_ID = -90;
  public static final long BATTERIES_WSS_ID = -95;

  public static final Jdbi jdbiTest;

  static {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:postgresql://localhost:5432/wnc_helene_test");
    config.setUsername("wnc_helene");
    config.setPassword("wnc_helene");
    config.addDataSourceProperty("maximumPoolSize", "16");
    HikariDataSource ds = new HikariDataSource(config);
    jdbiTest = Jdbi.create(ds);
  }

  public static void setupDatabase() {
    try {
      var sql = TestDataFile.TEST_DATA_SCHEMA.readData();
      TestConfiguration.jdbiTest.withHandle(handle -> handle.createScript(sql).execute());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Adds a new site with a random name, returns the name of the site. */
  public static String addSite() {
    return addSite(SiteType.DISTRIBUTION_CENTER);
  }

  public static String addSite(SiteType siteType) {
    String name = "test-name " + UUID.randomUUID().toString();
    AddSiteDao.addSite(
        jdbiTest,
        AddSiteData.builder()
            .siteName(name)
            .county("Watauga")
            .state("NC")
            .city("city " + name)
            .streetAddress("address of " + name)
            .siteType(siteType)
            .maxSupplyLoad("Car")
            .contactNumber("000")
            .build());
    return name;
  }

  public static void addCounty(String county, String state) {
    String insert = "insert into county(name, state) values (:name, :state)";
    jdbiTest.withHandle(
        handle -> handle.createUpdate(insert).bind("name", county).bind("state", state).execute());
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

  public static void addItemToSite(
      long siteId, ItemStatus itemStatus, String itemName, long wssId) {
    String insert =
        """
        insert into site_item(site_id, item_id, item_status_id, wss_id)
        values(
          :siteId,
          (select id from item where name = :itemName),
          (select id from item_status where name = :itemStatus),
          :wssId
        )
        """;
    TestConfiguration.jdbiTest.withHandle(
        handle ->
            handle
                .createUpdate(insert)
                .bind("siteId", siteId)
                .bind("itemStatus", itemStatus.getText())
                .bind("itemName", itemName)
                .bind("wssId", wssId)
                .execute());
  }
}
