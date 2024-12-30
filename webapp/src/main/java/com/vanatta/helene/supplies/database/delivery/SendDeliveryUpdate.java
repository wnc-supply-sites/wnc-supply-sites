package com.vanatta.helene.supplies.database.delivery;

import com.vanatta.helene.supplies.database.util.HttpPostSender;
import jakarta.annotation.Nonnull;
import lombok.Builder;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Sends an updated delivery status to Airtable. */
@Component
public class SendDeliveryUpdate {

  public static SendDeliveryUpdate disabled() {
    return new SendDeliveryUpdate(null, null, false, null);
  }

  @Builder
  @lombok.Value
  public static class UpdateDeliveryJson {
    @Nonnull Long airtableId;
    @Nonnull String deliveryStatus;
    @Nonnull String driverStatus;
    @Nonnull String driverConfirmLink;
    @Nonnull String pickupConfirmLink;
    @Nonnull String dropOffConfirmLink;
  }

  private final Jdbi jdbi;
  private final String websiteUri;
  private final boolean enabled;
  private final String airtableWebhookUrl;

  SendDeliveryUpdate(
      Jdbi jdbi,
      @Value("{website.uri}") String websiteUri,
      @Value("${make.enabled}") boolean enabled,
      @Value("${airtable.webhook.delivery.update}") String airtableWebhookUrl) {

    this.jdbi = jdbi;
    this.websiteUri = websiteUri;
    this.enabled = enabled;
    this.airtableWebhookUrl = airtableWebhookUrl;
  }

  void send(String publicKey, DeliveryStatus newStatus) {
    if (!enabled) {
      return;
    }
    UpdateDeliveryJson updateDeliveryJson = createPayload(jdbi, publicKey, newStatus);
    HttpPostSender.sendAsJson(airtableWebhookUrl, updateDeliveryJson);
  }

  UpdateDeliveryJson createPayload(Jdbi jdbi, String publicKey, DeliveryStatus newStatus) {
    Delivery delivery =
        DeliveryDao.fetchDeliveryByPublicKey(jdbi, publicKey)
            .orElseThrow(
                () -> new IllegalStateException("No delivery for public key: " + publicKey));

    return UpdateDeliveryJson.builder()
        .airtableId(delivery.getDeliveryNumber())
        .deliveryStatus(newStatus.getAirtableName())
        .driverStatus(delivery.getDriverStatus())
        .driverConfirmLink(
            websiteUri
                + DeliveryController.buildDeliveryPageLinkWithCode(
                    publicKey,
                    delivery
                        .getConfirmation(DeliveryConfirmation.ConfirmRole.DRIVER)
                        .orElseThrow()
                        .getCode()))
        .pickupConfirmLink(
            websiteUri
                + DeliveryController.buildDeliveryPageLinkWithCode(
                    publicKey,
                    delivery
                        .getConfirmation(DeliveryConfirmation.ConfirmRole.PICKUP_SITE)
                        .orElseThrow()
                        .getCode()))
        .dropOffConfirmLink(
            websiteUri
                + DeliveryController.buildDeliveryPageLinkWithCode(
                    publicKey,
                    delivery
                        .getConfirmation(DeliveryConfirmation.ConfirmRole.DROPOFF_SITE)
                        .orElseThrow()
                        .getCode()))
        .build();
  }
}
