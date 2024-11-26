package com.vanatta.helene.supplies.database.data.export;

import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExportToMakeConfiguration {


  @Bean
  SendSiteUpdate sendSiteUpdate(
      Jdbi jdbi,
      @Value("${make.webhook.upsertSite}") String siteUpsertWebhook
      ) {
    return new SendSiteUpdate(jdbi, siteUpsertWebhook);
  }

}
