package com.vanatta.helene.supplies.database.delivery;

import com.google.gson.Gson;
import com.vanatta.helene.supplies.database.manage.inventory.InventoryDao;
import com.vanatta.helene.supplies.database.util.TruncateString;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

/** Has a webhook for incoming requests from airtable to receive status updates for deliveries. */
@Controller
@Slf4j
@AllArgsConstructor
public class DeliveryController {

  /** How many items we can have in one column before we split the item list into two. */
  // @VisibleForTesting
  static int ITEM_LIST_ONE_COLUMN_MAX = 5;

  // @VisibleForTesting
  static int ITEM_LIST_TWO_COLUMN_MAX = 11;

  public static String buildDeliveryPageLink(String publicUrlKey) {
    return "/delivery/" + publicUrlKey;
  }

  // also does delivery upserts
  private static final String PATH_UPDATE_DELIVERY = "/webhook/update-delivery";

  private final Jdbi jdbi;

  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DeliveryUpdate {
    long deliveryId;
    String publicUrlKey;
    String deliveryStatus;
    List<String> dispatcherName;
    List<String> dispatcherNumber;
    List<String> driverName;
    List<String> driverNumber;
    List<Long> dropOffSiteWssId;
    List<Long> pickupSiteWssId;
    List<Long> itemListWssIds;
    List<String> licensePlateNumbers;
    String targetDeliveryDate;
    String dispatcherNotes;

    static DeliveryUpdate parseJson(String inputJson) {
      return new Gson().fromJson(inputJson, DeliveryUpdate.class);
    }

    boolean isComplete() {
      return deliveryStatus != null && deliveryStatus.toLowerCase().contains("complete");
    }
  }

  @PostMapping(PATH_UPDATE_DELIVERY)
  ResponseEntity<String> upsertDelivery(@RequestBody String body) {
    log.info("Delivery update endpoint received: {}", body);
    DeliveryUpdate deliveryUpdate = DeliveryUpdate.parseJson(body);

    // if either to or from site are gone, then we can remove the delivery.
    if (deliveryUpdate.getPickupSiteWssId().isEmpty()
        || deliveryUpdate.getDropOffSiteWssId().isEmpty()) {
      DeliveryDao.deleteDelivery(jdbi, deliveryUpdate.deliveryId);
    } else {
      DeliveryDao.upsert(jdbi, deliveryUpdate);
      if (deliveryUpdate.isComplete() && !deliveryUpdate.getItemListWssIds().isEmpty()) {
        log.info(
            "Delivery completion received! Updating site inventory items to no longer be needed." +
               "Site WSS ID: {}, item WSS IDs: {}",
            deliveryUpdate.dropOffSiteWssId,
            deliveryUpdate.getItemListWssIds());
        InventoryDao.markItemsAsNotNeeded(
            jdbi, deliveryUpdate.dropOffSiteWssId.getFirst(), deliveryUpdate.getItemListWssIds());
      }
    }

    return ResponseEntity.ok("ok");
  }

  enum TemplateParams {
    deliveryId,
    deliveryDate,
    itemCount,
    driverName,
    driverPhone,
    licensePlate,

    dispatcherName,
    dispatcherPhone,

    fromSiteName,
    fromSiteLink,
    fromAddress,
    fromAddressLine2,
    fromContactName,
    fromContactPhone,
    fromHours,

    toSiteName,
    toSiteLink,
    toAddress,
    toAddressLine2,
    toContactName,
    toContactPhone,
    toHours,

    items1,
    items2,
    items3;
  }

  @GetMapping("/delivery/{publicUrlKey}")
  ModelAndView showDeliveryDetailPage(@PathVariable("publicUrlKey") String publicUrlKey) {
    Map<String, Object> templateParams = new HashMap<>();

    Delivery delivery = DeliveryDao.fetchDeliveryByPublicKey(jdbi, publicUrlKey);
    templateParams.put(TemplateParams.deliveryId.name(), delivery.getDeliveryNumber());
    templateParams.put(TemplateParams.deliveryDate.name(), nullsToDash(delivery.getDeliveryDate()));
    templateParams.put(TemplateParams.itemCount.name(), delivery.getItemCount());
    templateParams.put(TemplateParams.driverName.name(), nullsToDash(delivery.getDriverName()));
    templateParams.put(TemplateParams.driverPhone.name(), delivery.getDriverNumber());
    templateParams.put(
        TemplateParams.licensePlate.name(), nullsToDash(delivery.getDriverLicensePlate()));
    templateParams.put(
        TemplateParams.dispatcherName.name(), nullsToDash(delivery.getDispatcherName()));
    templateParams.put(TemplateParams.dispatcherPhone.name(), delivery.getDispatcherNumber());
    templateParams.put(
        TemplateParams.fromSiteName.name(), TruncateString.truncate(delivery.getFromSite(), 30));
    templateParams.put(TemplateParams.fromSiteLink.name(), delivery.getFromSiteLink());
    templateParams.put(TemplateParams.fromAddress.name(), delivery.getFromAddress());
    templateParams.put(
        TemplateParams.fromAddressLine2.name(),
        delivery.getFromCity() + ", " + delivery.getFromState());
    templateParams.put(
        TemplateParams.fromContactName.name(), nullsToDash(delivery.getFromContactName()));
    templateParams.put(TemplateParams.fromContactPhone.name(), delivery.getFromContactPhone());
    templateParams.put(TemplateParams.fromHours.name(), nullsToDash(delivery.getFromHours()));
    templateParams.put(
        TemplateParams.toSiteName.name(), TruncateString.truncate(delivery.getToSite(), 30));
    templateParams.put(TemplateParams.toSiteLink.name(), delivery.getToSiteLink());
    templateParams.put(TemplateParams.toAddress.name(), nullsToDash(delivery.getToAddress()));
    templateParams.put(
        TemplateParams.toAddressLine2.name(), delivery.getToCity() + ", " + delivery.getToState());
    templateParams.put(
        TemplateParams.toContactName.name(), nullsToDash(delivery.getToContactName()));
    templateParams.put(
        TemplateParams.toContactPhone.name(), nullsToDash(delivery.getToContactPhone()));
    templateParams.put(TemplateParams.toHours.name(), nullsToDash(delivery.getToHours()));

    List<List<String>> split = splitItemList(delivery.getItemList().stream().sorted().toList());
    assert split.size() == 3;

    templateParams.put(TemplateParams.items1.name(), split.get(0));
    templateParams.put(TemplateParams.items2.name(), split.get(1));
    templateParams.put(TemplateParams.items3.name(), split.get(2));

    return new ModelAndView("delivery/delivery", templateParams);
  }

  private static String nullsToDash(String input) {
    return Optional.ofNullable(input).orElse("-");
  }

  // @VisibleForTesting
  static List<List<String>> splitItemList(List<String> items) {
    assert items != null;

    if (items.size() <= ITEM_LIST_ONE_COLUMN_MAX) {
      return List.of(items, List.of(), List.of());
    } else if (items.size() <= ITEM_LIST_TWO_COLUMN_MAX) {
      int splitLocation = items.size() % 2 == 0 ? items.size() / 2 : (items.size() / 2) + 1;
      List<String> one = items.subList(0, splitLocation);
      List<String> two = items.subList(splitLocation, items.size());
      return List.of(one, two, List.of());
    } else {

      int firstListSize = items.size() % 3 == 0 ? items.size() / 3 : (items.size() / 3) + 1;
      int secondListSize = items.size() % 3 == 2 ? (items.size() / 3) + 1 : items.size() / 3;
      int thirdListSize = items.size() / 3;
      assert (firstListSize + secondListSize + thirdListSize) == items.size();

      List<String> one = items.subList(0, firstListSize);
      List<String> two = items.subList(firstListSize, firstListSize + secondListSize);
      List<String> three = items.subList(firstListSize + secondListSize, items.size());
      return List.of(one, two, three);
    }
  }
}
