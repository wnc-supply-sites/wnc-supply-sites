package com.vanatta.helene.supplies.database.data.export;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Bean;

/**
 * Sends updates site to "Make.com"
 */
@Slf4j
@AllArgsConstructor
public class SendSiteUpdate {

  private final Jdbi jdbi;
  private final String webhookUrl;

  public void send(long siteId) {

    new Thread(() -> {
      DataExportDao.SiteExportData siteExportData = DataExportDao.lookupSite(jdbi, siteId);
      log.info("Sending JSON data to make for site upsert: " + new Gson().toJson(siteExportData));

    }).start();
//    log.info(new Gson().toJson(siteData));
  }
}
