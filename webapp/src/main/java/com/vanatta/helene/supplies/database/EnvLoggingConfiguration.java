package com.vanatta.helene.supplies.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/** This config class just logs out the state of all application.properties values. */
@Configuration
@Slf4j
public class EnvLoggingConfiguration {

  EnvLoggingConfiguration(
      @Value("${jdbi.url}") String url,
      @Value("${jdbi.user}") String user,
      @Value("${auth.enabled}") boolean authEnabled,
      @Value("${make.enabled}") boolean makeEnabled,
      @Value("${make.webhook.upsertSite}") String upsertWebhook,
      @Value("${make.webhook.newItem}") String newItemWebhook,
      @Value("${make.webhook.updateInventory}") String updateInventoryWebhook,
      @Value("${make.webhook.dispatch.new}") String dispatchWebhook,
      @Value("${distance.calculator.enabled}") boolean distanceCalculatorEnabled,
      @Value("${distance.calculator.delay.ms}") int distanceCalculatorDelayMs,
      @Value("${google.maps.api.key}") String googleApiKey) {
    log.info("ENV - JDBI URL: {}", url);
    log.info("ENV - JDBI USER: {}", user);
    log.info("ENV - AUTH ENABLED: {}", authEnabled);
    log.info("ENV - MAKE ENABLED: {}", makeEnabled);
    log.info("ENV - UPSERT WEBHOOK: {}", upsertWebhook);
    log.info("ENV - NEW ITEM WEBHOOK: {}", newItemWebhook);
    log.info("ENV - UPDATE INVENTORY WEBHOOK: {}", updateInventoryWebhook);
    log.info("ENV - DISPATCH WEBHOOK: {}", dispatchWebhook);
    log.info("ENV - DISTANCE CALCULATOR ENABLED: {}", distanceCalculatorEnabled);
    log.info("ENV - DISTANCE CALCULATOR DELAY: {}", distanceCalculatorDelayMs);
    log.info("ENV - GOOGLE API KEY: {}", googleApiKey);
  }
}
