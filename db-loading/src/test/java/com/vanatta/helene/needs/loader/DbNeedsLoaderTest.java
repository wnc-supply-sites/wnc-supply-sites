package com.vanatta.helene.needs.loader;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class DbNeedsLoaderTest {

  @BeforeAll
  static void clearDatabase() {
    DbNeedsLoader.jdbiTest.withHandle(handle -> handle.createUpdate("delete from site_item").execute());
    DbNeedsLoader.jdbiTest.withHandle(handle -> handle.createUpdate("delete from site").execute());
    DbNeedsLoader.jdbiTest.withHandle(handle -> handle.createUpdate("delete from item").execute());
  }

  private static final CsvDistroData data =
      CsvDistroData.builder()
          .organizationName("org name")
          .donationCenterStatus("accepting donations")
          .streetAddress("106 address")
          .city("city")
          .county("Ashe")
          .state("NC")
          .zipCode("98000")
          .kidsToys("toy1\ntoy2")
          .cleanup("bleach")
          .food("canned beans\ncanned fruit")
          .build();

  @Test
  void populateData() {
    DbNeedsLoader.populate(DbNeedsLoader.jdbiTest, List.of(data));

    String countItems =
        """
      select count(*) from site_item where
         site_id = (select id from site where name = 'org name')
      """;
    int count =
        DbNeedsLoader.jdbiTest.withHandle(
            handle -> handle.createQuery(countItems).mapTo(Integer.class).one());

    Assertions.assertThat(count).isEqualTo(5);
  }
}
