package com.vanatta.helene.supplies.database.export.update;

import com.vanatta.helene.supplies.database.util.HttpPostSender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

// TODO: test-me
/**
 * Whenever inventory is updated for a site, we send updates to Make. Inventory is updated when it
 * is added or removed to a site, or when its status is changed.
 */
@AllArgsConstructor
public class SendInventoryUpdate {

  private final Jdbi jdbi;
  private final String webhookUrl;
  private final boolean enabled;

  public void send(long siteId, String itemName) {
    if (enabled) {
      SiteDataDbResult siteDataDbResult = fetchItemForSite(jdbi, siteId, itemName);
      HttpPostSender.sendAsJson(webhookUrl, siteDataDbResult);
    }
  }

  static SiteDataDbResult fetchItemForSite(Jdbi jdbi, long siteId, String itemName) {
    String query =
        """
          select
            s.name site_name,
            i.name item_name,
            si.wss_id itemNeedWssId,
            its.name item_status
          from site s
          left join site_item si on s.id = si.site_id
          left join item i on i.id = si.item_id
          left join item_status its on its.id = si.item_status_id
          where s.id = :siteId and i.name = :itemName
          """;

    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(query)
                .bind("siteId", siteId)
                .bind("itemName", itemName)
                .mapToBean(SiteDataDbResult.class)
                .one());
  }

  public void sendItemRemoval(long wssId) {
    if (enabled) {
      var dataToSend =
          SiteDataDbResult.builder().itemStatus("Removed").itemNeedWssId(wssId).build();
      HttpPostSender.sendAsJson(webhookUrl, dataToSend);
    }
  }

  /** Represents DB data for one site with all of its inventory availability and needs. */
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SiteDataDbResult {
    String siteName;
    String itemName;
    long itemNeedWssId;
    String itemStatus;
    @Builder.Default long updateTimeStamp = System.currentTimeMillis();
  }
}
