package com.vanatta.helene.supplies.database.export.update;

import com.vanatta.helene.supplies.database.util.HttpPostSender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

/** Sends new item updates to 'Make', which is then sent to Airtable. */
@AllArgsConstructor
public class SendNewItemUpdate {
  private final Jdbi jdbi;
  private final String webhookUrl;
  private final boolean enabled;

  public void sendNewItem(String itemName) {
    if (enabled) {
      // do not send this request on a new thread.
      // We need to be sure that this request is the first to be sent out before we send
      // another request to attach the item to a site.
      ItemFromDatabase item = lookupItem(jdbi, itemName);
      HttpPostSender.sendAsJson(webhookUrl, item);
    }
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ItemFromDatabase {
    String name;
    Long wssId;
  }

  //  @VisibleForTesting
  static ItemFromDatabase lookupItem(Jdbi jdbi, String itemName) {
    String query = "select name, wss_id from item where name = :itemName";

    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(query)
                .bind("itemName", itemName)
                .mapToBean(ItemFromDatabase.class)
                .one());
  }
}
