package com.vanatta.helene.supplies.database.export.update;

import com.vanatta.helene.supplies.database.export.bulk.BulkDataExportDao;
import com.vanatta.helene.supplies.database.util.HttpPostSender;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;

@AllArgsConstructor
public class SendInventoryUpdate {

  private final Jdbi jdbi;
  private final String webhookUrl;
  private final boolean enabled;

  public void send(long siteId) {
    if (enabled) {
      BulkDataExportDao.SiteItemExportJson siteItemExportJson =
          BulkDataExportDao.fetchAllSiteItemsForSite(jdbi, siteId);
      HttpPostSender.sendAsJson(webhookUrl, siteItemExportJson);
    }
  }
}
