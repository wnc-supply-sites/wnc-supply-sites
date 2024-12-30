package com.vanatta.helene.supplies.database.delivery;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;

import com.vanatta.helene.supplies.database.test.util.TestDataFile;

public class DeliveryHelper {

  static Delivery withDispatcherConfirmedDelivery() {
    var update = DeliveryUpdate.parseJson(TestDataFile.DELIVERY_DATA_JSON.readData());
    DeliveryDao.upsert(jdbiTest, update);
    ConfirmationDao.dispatcherConfirm(jdbiTest, update.getPublicUrlKey());
    return DeliveryDao.fetchDeliveryByPublicKey(jdbiTest, update.getPublicUrlKey()).orElseThrow();
  }

  static Delivery withConfirmedDelivery() {
    var update = DeliveryUpdate.parseJson(TestDataFile.DELIVERY_DATA_JSON.readData());
    DeliveryDao.upsert(jdbiTest, update);
    ConfirmationDao.dispatcherConfirm(jdbiTest, update.getPublicUrlKey());

    Delivery delivery =
        DeliveryDao.fetchDeliveryByPublicKey(jdbiTest, update.getPublicUrlKey()).orElseThrow();

    delivery
        .getConfirmations()
        .forEach(
            confirmartion ->
                ConfirmationDao.confirmDelivery(
                    jdbiTest,
                    delivery.getPublicKey(),
                    DeliveryConfirmation.ConfirmRole.valueOf(confirmartion.getConfirmRole())));

    return delivery;
  }

  public static Delivery withNewDelivery() {
    var update = DeliveryUpdate.parseJson(TestDataFile.DELIVERY_DATA_JSON.readData());
    DeliveryDao.upsert(jdbiTest, update);
    return DeliveryDao.fetchDeliveryByPublicKey(jdbiTest, update.getPublicUrlKey()).orElseThrow();
  }
}
