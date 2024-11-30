package com.vanatta.helene.supplies.database.export;

import com.vanatta.helene.supplies.database.export.bulk.DataExportDao;
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
      DataExportDao.SiteItemExportJson siteItemExportJson =
          DataExportDao.fetchAllSiteItemsForSite(jdbi, siteId);
      HttpPostSender.sendAsJson(webhookUrl, siteItemExportJson);
    }
  }
}
