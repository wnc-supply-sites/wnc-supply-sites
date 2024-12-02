package com.vanatta.helene.supplies.database.incoming.webhook.need.request;

import com.vanatta.helene.supplies.database.util.TrimUtil;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@AllArgsConstructor
public class InventoryItemImportController {

  private final Jdbi jdbi;

  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ItemImport {
    Long airtableId;
    String itemName;
    List<String> descriptionTags;

    static ItemImport clean(ItemImport itemImport) {
      if (itemImport.isMissingData()) {
        throw new IllegalStateException();
      }
      return itemImport.toBuilder()
          .itemName(TrimUtil.trim(itemImport.getItemName()))
          .descriptionTags(TrimUtil.trim(itemImport.getDescriptionTags()))
          .build();
    }

    boolean isMissingData() {
      return airtableId == null || itemName == null || itemName.isBlank();
    }
  }

  @PostMapping("/import/update/inventory-item")
  ResponseEntity<String> updateInventoryItem(@RequestBody ItemImport itemImport) {
    log.info("Received import data: {}", itemImport);
    if (itemImport.isMissingData()) {
      log.warn("Bad data received for update inventory-item: {}", itemImport);
      return ResponseEntity.badRequest().body("Missing data");
    }

    itemImport = ItemImport.clean(itemImport);

    // first try to update the item by an ID - if that fails then we'll update it by name. If both
    // fail, then it's a new item and we insert it.

    int updateCountById = updateItemByAirtableId(itemImport);
    if (updateCountById == 0) {
      int updateCountByName = updateItemByName(itemImport);
      if (updateCountByName == 0) {
        insertItem(itemImport);
      }
    }

    return ResponseEntity.ok().build();
  }

  private int updateItemByAirtableId(ItemImport itemImport) {
    String update =
        """
            update item set name = :name, last_updated = now() where airtable_id = :airtableId
            """;
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(update)
                .bind("name", itemImport.getItemName())
                .bind("airtableId", itemImport.getAirtableId())
                .execute());
  }

  private int updateItemByName(ItemImport itemImport) {
    String update =
        """
            update item set airtable_id = :airtableId, last_updated = now() where name = :name
            """;
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(update)
                .bind("airtableId", itemImport.getAirtableId())
                .bind("name", itemImport.getItemName())
                .execute());
  }

  private void insertItem(ItemImport itemImport) {
    String insert =
        """
            insert into item(name, airtable_id)
            values(:name, :airtableId)
            """;
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(insert)
                .bind("airtableId", itemImport.getAirtableId())
                .bind("name", itemImport.getItemName())
                .execute());
  }
}
