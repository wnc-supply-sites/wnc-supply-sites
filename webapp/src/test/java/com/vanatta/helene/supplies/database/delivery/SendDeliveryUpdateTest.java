package com.vanatta.helene.supplies.database.delivery;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;
import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.DomainName;
import com.vanatta.helene.supplies.database.TestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SendDeliveryUpdateTest {
  SendDeliveryUpdate sendDeliveryUpdate = new SendDeliveryUpdate(jdbiTest, false, "");

  @BeforeEach
  void setup() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void createPayload() {
    var delivery = DeliveryHelper.withConfirmedDelivery();

    var payload =
        sendDeliveryUpdate.createPayload(
            jdbiTest, delivery.getPublicKey(), DeliveryStatus.DELIVERY_IN_PROGRESS);

    assertThat(payload.getAirtableId()).isEqualTo(delivery.getDeliveryNumber());
    assertThat(payload.getDeliveryStatus())
        .isEqualTo(DeliveryStatus.DELIVERY_IN_PROGRESS.getAirtableName());
    assertThat(payload.getDriverStatus()).isEqualTo(delivery.getDriverStatus());

    assertThat(payload.getPickupConfirmLink())
        .isEqualTo(
            String.format(
                "https://%s/delivery/%s?code=%s",
                DomainName.DOMAIN_NAME,
                delivery.getPublicKey(),
                delivery
                    .getConfirmation(DeliveryConfirmation.ConfirmRole.PICKUP_SITE)
                    .orElseThrow()
                    .getCode()));

    assertThat(payload.getDropOffConfirmLink())
        .isEqualTo(
            String.format(
                "https://%s/delivery/%s?code=%s",
                DomainName.DOMAIN_NAME,
                delivery.getPublicKey(),
                delivery
                    .getConfirmation(DeliveryConfirmation.ConfirmRole.DROPOFF_SITE)
                    .orElseThrow()
                    .getCode()));

    assertThat(payload.getDriverConfirmLink())
        .isEqualTo(
            String.format(
                "https://%s/delivery/%s?code=%s",
                DomainName.DOMAIN_NAME,
                delivery.getPublicKey(),
                delivery
                    .getConfirmation(DeliveryConfirmation.ConfirmRole.DRIVER)
                    .orElseThrow()
                    .getCode()));
  }
}
