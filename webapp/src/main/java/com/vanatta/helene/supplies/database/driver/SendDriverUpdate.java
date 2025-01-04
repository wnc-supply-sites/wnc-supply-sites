package com.vanatta.helene.supplies.database.driver;

import com.vanatta.helene.supplies.database.util.HttpPostSender;
import com.vanatta.helene.supplies.database.util.ThreadRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SendDriverUpdate {

  static SendDriverUpdate disabled() {
    return new SendDriverUpdate(false, null);
  }

  final boolean enabled;
  final String airtableWebhookUrl;

  SendDriverUpdate(
      @Value("${make.enabled}") boolean makeEnabled,
      @Value("${airtable.webhook.driver.update}") String updateDriverWebhook) {
    this.airtableWebhookUrl = updateDriverWebhook;
    this.enabled = makeEnabled;
  }

  void sendUpdate(Driver driver) {
    if (!enabled) {
      return;
    }

    ThreadRunner.run(() -> HttpPostSender.sendAsJson(airtableWebhookUrl, driver));
  }
}
