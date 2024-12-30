package com.vanatta.helene.supplies.database.delivery;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;

import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
import com.vanatta.helene.supplies.database.test.util.TestDataFile;
import java.util.List;

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

  public static Delivery withNewDelivery(long fromSiteId, long toSiteId) {
    long fromSiteWssId = SiteDetailDao.lookupSiteById(jdbiTest, fromSiteId).getWssId();
    long toSiteWssId = SiteDetailDao.lookupSiteById(jdbiTest, toSiteId).getWssId();

    var update =
        DeliveryUpdate.parseJson(TestDataFile.DELIVERY_DATA_JSON.readData()).toBuilder()
            .dropOffSiteWssId(List.of(toSiteWssId))
            .pickupSiteWssId(List.of(fromSiteWssId))
            .build();
    DeliveryDao.upsert(jdbiTest, update);
    return DeliveryDao.fetchDeliveryByPublicKey(jdbiTest, update.getPublicUrlKey()).orElseThrow();
  }
}
