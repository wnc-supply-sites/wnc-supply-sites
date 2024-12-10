package com.vanatta.helene.supplies.database.export.update;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import org.junit.jupiter.api.Test;

class SendNewItemUpdateTest {

  @Test
  void findItemInDatabase() {
    var result = SendNewItemUpdate.lookupItem(TestConfiguration.jdbiTest, "gloves");
    assertThat(result.getWssId()).isNotNull();
    assertThat(result.getName()).isNotNull();
  }
}
