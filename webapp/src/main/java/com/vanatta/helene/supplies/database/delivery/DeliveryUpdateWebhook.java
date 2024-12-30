package com.vanatta.helene.supplies.database.delivery;

import com.vanatta.helene.supplies.database.manage.inventory.InventoryDao;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Webhook to receive delivery updates from airtable. */
@Controller
@Slf4j
@AllArgsConstructor
class DeliveryUpdateWebhook {

  // also does delivery upserts
  private static final String PATH_UPDATE_DELIVERY = "/webhook/update-delivery";

  private final Jdbi jdbi;

  @PostMapping(PATH_UPDATE_DELIVERY)
  ResponseEntity<String> upsertDelivery(@RequestBody String body) {
    log.info("Delivery update endpoint received: {}", body);
    DeliveryUpdate deliveryUpdate = DeliveryUpdate.parseJson(body);

    String oldStatus =
        DeliveryDao.fetchDeliveryByPublicKey(jdbi, deliveryUpdate.getPublicUrlKey())
            .map(Delivery::getDeliveryStatus)
            .orElse("");

    DeliveryDao.upsert(jdbi, deliveryUpdate);

    // if the delivery was already completed, and we get an update and the delivery is still
    // complete, then
    // we should skip any automations.
    boolean deliveryWasNotComplete = !oldStatus.toLowerCase().contains("complete");
    boolean deliveryIsNowComplete = deliveryUpdate.isComplete();
    boolean deliveryContainsItems = !deliveryUpdate.getItemListWssIds().isEmpty();
    if (deliveryWasNotComplete && deliveryIsNowComplete && deliveryContainsItems) {
      log.info(
          "Delivery completion received! Updating site inventory items to no longer be needed."
              + "Site WSS ID: {}, item WSS IDs: {}",
          deliveryUpdate.dropOffSiteWssId,
          deliveryUpdate.getItemListWssIds());
      if (!deliveryUpdate.dropOffSiteWssId.isEmpty()) {
        InventoryDao.markItemsAsNotNeeded(
            jdbi, deliveryUpdate.dropOffSiteWssId.getFirst(), deliveryUpdate.getItemListWssIds());
      }
    }

    return ResponseEntity.ok("ok");
  }
}
