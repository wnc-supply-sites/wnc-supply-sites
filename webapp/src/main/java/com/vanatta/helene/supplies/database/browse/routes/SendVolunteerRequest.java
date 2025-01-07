package com.vanatta.helene.supplies.database.browse.routes;

import com.vanatta.helene.supplies.database.util.HttpPostSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SendVolunteerRequest {

  static SendVolunteerRequest disabled() {
    return new SendVolunteerRequest(false, "");
  }

  private final boolean enabled;
  private final String webhookUrl;

  SendVolunteerRequest(
      @Value("${make.enabled}") boolean enabled,
      @Value("${airtable.webhook.delivery.volunteer}") String deliveryVolunteerWebhook) {

    this.enabled = enabled;
    this.webhookUrl = deliveryVolunteerWebhook;
  }

  public void send(RouteVolunteeringController.DeliveryVolunteerRequest json) {
    if (!enabled) {
      log.info("Send to airtable disabled, would have sent: {}", json);
    } else {
      HttpPostSender.sendAsJson(webhookUrl, json);
    }
  }
}
