package com.vanatta.helene.supplies.database.export.update;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SendInventoryUpdateTest {

  @BeforeAll
  static void setUp() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void query() {
    var result =
        SendInventoryUpdate.fetchItemForSite(
            TestConfiguration.jdbiTest, TestConfiguration.getSiteId("site1"), "new clothes");

    assertThat(result.getItemName()).isEqualTo("new clothes");
    assertThat(result.getSiteName()).isEqualTo("site1");
    assertThat(result.getItemNeedWssId()).isEqualTo(TestConfiguration.SITE1_WSS_ID);
    assertThat(result.getItemStatus()).isEqualTo("Urgently Needed");
  }

  /** When requesting an item no longer assigned to a site, we should get a "removed" record. */
  @Test
  void removeSiteItem() {
    var result =
        SendInventoryUpdate.fetchItemForSite(
            TestConfiguration.jdbiTest,
            TestConfiguration.getSiteId("site1"),
            "some item that is not in the database");

    assertThat(result.getItemName()).isEqualTo("some item that is not in the database");
    assertThat(result.getSiteName()).isEqualTo("site1");
    assertThat(result.getItemNeedWssId()).isEqualTo(TestConfiguration.SITE1_WSS_ID);
    assertThat(result.getItemStatus()).isEqualTo("Removed");
  }
}
