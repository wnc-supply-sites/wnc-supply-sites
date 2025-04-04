package com.vanatta.helene.supplies.database.delivery;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.vanatta.helene.supplies.database.TestConfiguration;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DeliveryDaoTest {

  static final String upsertJson1 =
      """
    {"deliveryId":90,"itemListWssIds":[277,168,283,191,192,207,208,296,221,242,253,314,315],
    "driverNumber":[],"driverName":[],"dispatcherNumber":["828.279.2054"],
    "dispatcherName":["John"],"deliveryStatus":"Assigning Driver",
    "dropOffSiteWssId":[99],"pickupSiteWssId":[101],
    "targetDeliveryDate":"2024-12-16","licensePlateNumbers":[],
    "publicUrlKey": "akey", "dispatcherCode":"TTTT"}
    """;

  static final String upsertJson2 =
      """
    {"deliveryId":95,"itemListWssIds":[],
    "driverNumber":[],"driverName":[],"dispatcherNumber":[],
    "dispatcherName":[],"deliveryStatus":"Assigning Driver",
    "dropOffSiteWssId":[99],"pickupSiteWssId":[101],
    "targetDeliveryDate":"","licensePlateNumbers":[],
    "publicUrlKey": "bkey", "dispatcherCode":"ZZZZ"}
    """;

  static final long SITE1_WSS_ID = -10;
  static final long SITE2_WSS_ID = -20;
  static final long WATER_WSS_ID = -40;
  static final long GLOVES_WSS_ID = -50;

  @BeforeAll
  static void setupDatabase() {
    TestConfiguration.setupDatabase();
  }

  @ParameterizedTest
  @ValueSource(strings = {upsertJson1, upsertJson2})
  void doUpserts(String inputJson) {
    DeliveryUpdate update =
        new Gson()
            .fromJson(inputJson, DeliveryUpdate.class).toBuilder()
                .pickupSiteWssId(List.of(SITE1_WSS_ID))
                .dropOffSiteWssId(List.of(SITE2_WSS_ID))
                .itemListWssIds(List.of(WATER_WSS_ID, GLOVES_WSS_ID))
                .build();

    DeliveryDao.upsert(jdbiTest, update);

    var driverCode =
        DeliveryDao.fetchDeliveryByPublicKey(jdbiTest, update.getPublicUrlKey())
            .orElseThrow()
            .getDriverCode();
    assertThat(driverCode).isNotNull();

    // validate that we can do another upsert
    DeliveryDao.upsert(jdbiTest, update);
    // check that the driver code does not change.
    assertThat(
            DeliveryDao.fetchDeliveryByPublicKey(jdbiTest, update.getPublicUrlKey())
                .orElseThrow()
                .getDriverCode())
        .isEqualTo(driverCode);
  }

  /** Make sure we can do a lookup of a delivery by public URL key */
  @ParameterizedTest
  @ValueSource(strings = {"BETA", "XKCD", "ABCD"})
  void fetchDeliveryByPublicUrl(String urlKey) {
    var result = DeliveryDao.fetchDeliveryByPublicKey(jdbiTest, urlKey).orElseThrow();
    assertThat(result).isNotNull();
  }

  @Test
  void updateDeliveryStatus() {
    var delivery = DeliveryHelper.withNewDelivery();

    DeliveryDao.updateDeliveryStatus(
        jdbiTest, delivery.getPublicKey(), DeliveryStatus.DELIVERY_IN_PROGRESS);
    var status =
        DeliveryDao.fetchDeliveryByPublicKey(jdbiTest, delivery.getPublicKey())
            .orElseThrow()
            .getDeliveryStatus();
    assertThat(status).isEqualTo(DeliveryStatus.DELIVERY_IN_PROGRESS.getAirtableName());

    DeliveryDao.updateDeliveryStatus(
        jdbiTest, delivery.getPublicKey(), DeliveryStatus.DELIVERY_CANCELLED);
    status =
        DeliveryDao.fetchDeliveryByPublicKey(jdbiTest, delivery.getPublicKey())
            .orElseThrow()
            .getDeliveryStatus();
    assertThat(status).isEqualTo(DeliveryStatus.DELIVERY_CANCELLED.getAirtableName());
  }
}
