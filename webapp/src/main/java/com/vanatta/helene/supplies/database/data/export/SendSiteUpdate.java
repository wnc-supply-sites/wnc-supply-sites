package com.vanatta.helene.supplies.database.data.export;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

/** Sends updates site to "Make.com" */
@Slf4j
@AllArgsConstructor
public class SendSiteUpdate {

  private final Jdbi jdbi;
  private final String webhookUrl;

  public void sendWithNameUpdate(long siteId, String oldName) {
    new Thread(
            () -> {
              DataExportDao.SiteExportData siteExportData = DataExportDao.lookupSite(jdbi, siteId);
              siteExportData.setOldName(oldName);
              HttpPostSender.sendAsJson(webhookUrl, siteExportData);
            })
        .start();
  }

  public void send(long siteId) {
    new Thread(
            () -> {
              DataExportDao.SiteExportData siteExportData = DataExportDao.lookupSite(jdbi, siteId);
              HttpPostSender.sendAsJson(webhookUrl, siteExportData);
            })
        .start();
  }
}
