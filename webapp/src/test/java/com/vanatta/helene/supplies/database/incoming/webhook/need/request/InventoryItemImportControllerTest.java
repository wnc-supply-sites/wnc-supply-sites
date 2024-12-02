package com.vanatta.helene.supplies.database.incoming.webhook.need.request;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;
import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class InventoryItemImportControllerTest {

  static class Helper {
    @Nullable
    static String getItemNameByAirTableId(Long airTableId) {
      return jdbiTest.withHandle(
          handle ->
              handle
                  .createQuery("select name from item where airtable_id = :airTableId ")
                  .bind("airTableId", airTableId)
                  .mapTo(String.class)
                  .findOne()
                  .orElse(null));
    }

    @Nullable
    static Long getAirtableIdByItemName(String itemName) {
      return jdbiTest.withHandle(
          handle ->
              handle
                  .createQuery("select airtable_id from item where name = :name ")
                  .bind("name", itemName)
                  .mapTo(Long.class)
                  .findOne()
                  .orElse(null));
    }
  }

  @BeforeAll
  static void setup() {
    TestConfiguration.setupDatabase();
    assertThat(Helper.getAirtableIdByItemName("gloves")).isNull();
  }

  InventoryItemImportController controller = new InventoryItemImportController(jdbiTest);

  @Test
  void update() {
    // do an update that will set the airtable ID
    ResponseEntity<String> response =
        controller.updateInventoryItem(
            InventoryItemImportController.ItemImport.builder()
                .airtableId(12L)
                .itemName("gloves")
                .build());

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(Helper.getAirtableIdByItemName("gloves")).isEqualTo(12L);
    assertThat(Helper.getItemNameByAirTableId(12L)).isEqualTo("gloves");

    // no-op - do the same update again
    response =
        controller.updateInventoryItem(
            InventoryItemImportController.ItemImport.builder()
                .airtableId(12L)
                .itemName("gloves")
                .build());
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(Helper.getAirtableIdByItemName("gloves")).isEqualTo(12L);
    assertThat(Helper.getItemNameByAirTableId(12L)).isEqualTo("gloves");

    // update name of the item via airtable id
    response =
        controller.updateInventoryItem(
            InventoryItemImportController.ItemImport.builder()
                .airtableId(12L)
                .itemName("work gloves")
                .build());
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(Helper.getAirtableIdByItemName("work gloves")).isEqualTo(12L);
    assertThat(Helper.getItemNameByAirTableId(12L)).isEqualTo("work gloves");
  }

  @Test
  void addBrandNewItem() {
    assertThat(Helper.getAirtableIdByItemName("brand-new-item")).isNull();
    assertThat(Helper.getItemNameByAirTableId(100L)).isNull();

    ResponseEntity<String> response =
        controller.updateInventoryItem(
            InventoryItemImportController.ItemImport.builder()
                .airtableId(100L)
                .itemName("brand-new-item")
                .build());

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(Helper.getAirtableIdByItemName("brand-new-item")).isEqualTo(100L);
    assertThat(Helper.getItemNameByAirTableId(100L)).isEqualTo("brand-new-item");
  }
}
