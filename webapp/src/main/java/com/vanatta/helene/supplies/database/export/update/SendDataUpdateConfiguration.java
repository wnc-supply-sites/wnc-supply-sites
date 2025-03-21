package com.vanatta.helene.supplies.database.export.update;

import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Config for classes that can be used to send data updates to Make. */
@Configuration
public class SendDataUpdateConfiguration {

  @Bean
  SendSiteUpdate sendSiteUpdate(
      Jdbi jdbi,
      @Value("${make.webhook.upsertSite}") String siteUpsertWebhook,
      @Value("${make.enabled}") boolean enabled) {
    return new SendSiteUpdate(jdbi, siteUpsertWebhook, enabled);
  }

  @Bean
  SendNewItemUpdate newItemUpdate(
      Jdbi jdbi,
      @Value("${make.enabled}") boolean enabled,
      @Value("${make.webhook.newItem}") String newItemWebhook) {
    return new SendNewItemUpdate(jdbi, newItemWebhook, enabled);
  }

  @Bean
  SendInventoryUpdate sendInventoryUpdate(
      Jdbi jdbi,
      @Value("${make.webhook.updateInventory}") String siteUpsertWebhook,
      @Value("${make.enabled}") boolean enabled) {
    return new SendInventoryUpdate(jdbi, siteUpsertWebhook, enabled);
  }
}
