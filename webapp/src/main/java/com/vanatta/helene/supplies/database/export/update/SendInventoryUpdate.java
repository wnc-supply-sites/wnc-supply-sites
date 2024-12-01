package com.vanatta.helene.supplies.database.export.update;

import com.vanatta.helene.supplies.database.export.bulk.BulkDataExportDao;
import com.vanatta.helene.supplies.database.util.HttpPostSender;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

/**
 * Whenever inventory is updated for a site, we send updates to Make. Inventory is updated when it
 * is added or removed to a site, or when its status is changed.
 */
@AllArgsConstructor
public class SendInventoryUpdate {

  private final Jdbi jdbi;
  private final String webhookUrl;
  private final boolean enabled;

  public void send(long siteId) {
    if (enabled) {
      SiteItemExportJson siteItemExportJson = fetchAllItemsForSite(jdbi, siteId);
      HttpPostSender.sendAsJson(webhookUrl, siteItemExportJson);
    }
  }

  /**
   * Class that can be converted to a JSON. Input are results from DB, which have comma delimited
   * values, we put those into lists.
   */
  @Data
  @NoArgsConstructor
  public static class SiteItemExportJson {
    String siteName;

    List<String> urgentlyNeeded;
    List<String> needed;
    List<String> available;
    List<String> oversupply;

    SiteItemExportJson(BulkDataExportDao.SiteItemResult result) {
      this.siteName = result.getSiteName();
      this.urgentlyNeeded =
          extractField(result, BulkDataExportDao.SiteItemResult::getUrgentlyNeeded);
      this.needed = extractField(result, BulkDataExportDao.SiteItemResult::getNeeded);
      this.available = extractField(result, BulkDataExportDao.SiteItemResult::getAvailable);
      this.oversupply = extractField(result, BulkDataExportDao.SiteItemResult::getOverSupply);
    }

    private static List<String> extractField(
        BulkDataExportDao.SiteItemResult result,
        Function<BulkDataExportDao.SiteItemResult, String> mapping) {
      String value = mapping.apply(result);
      return value == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(value.split(",")));
    }
  }

  static SiteItemExportJson fetchAllItemsForSite(Jdbi jdbi, long siteId) {
    String query =
        """
          select
            s.name site_name,
            string_agg(i.name, ',') filter (where its.name in ('Urgently Needed')) urgentlyNeeded,
            string_agg(i.name, ',') filter (where its.name in ('Needed')) needed,
            string_agg(i.name, ',') filter (where its.name in ('Available')) available,
            string_agg(i.name, ',') filter (where its.name in ('Oversupply')) oversupply
          from site s
          left join site_item si on s.id = si.site_id
          left join item i on i.id = si.item_id
          left join item_status its on its.id = si.item_status_id
      where s.id = :siteId
    group by site_name
    """;

    var result =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(query)
                    .bind("siteId", siteId)
                    .mapToBean(BulkDataExportDao.SiteItemResult.class)
                    .one());
    return new SiteItemExportJson(result);
  }
}
