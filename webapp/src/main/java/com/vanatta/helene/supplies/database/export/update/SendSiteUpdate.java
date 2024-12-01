package com.vanatta.helene.supplies.database.export.update;

import com.vanatta.helene.supplies.database.export.bulk.BulkDataExportDao;
import com.vanatta.helene.supplies.database.util.HttpPostSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

/** Sends updates site to "Make.com" */
@Slf4j
@AllArgsConstructor
public class SendSiteUpdate {

  private final Jdbi jdbi;
  private final String webhookUrl;
  private final boolean enabled;

  public void sendWithNameUpdate(long siteId, String oldName) {
    if(!enabled) {
      return;
    }
    new Thread(
            () -> {
              BulkDataExportDao.SiteExportJson siteExportJson = BulkDataExportDao.lookupSite(jdbi, siteId);
              siteExportJson.setOldName(oldName);
              HttpPostSender.sendAsJson(webhookUrl, siteExportJson);
            })
        .start();
  }

  public void send(long siteId) {
    if(!enabled) {
      return;
    }
    new Thread(
            () -> {
              BulkDataExportDao.SiteExportJson siteExportJson = BulkDataExportDao.lookupSite(jdbi, siteId);
              HttpPostSender.sendAsJson(webhookUrl, siteExportJson);
            })
        .start();
  }
}
