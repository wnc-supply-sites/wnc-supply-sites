package com.vanatta.helene.supplies.database.manage.item.management;

import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.dispatch.DispatchRequestService;
import com.vanatta.helene.supplies.database.export.NewItemUpdate;
import com.vanatta.helene.supplies.database.export.SendInventoryUpdate;
import com.vanatta.helene.supplies.database.export.SendSiteUpdate;
import com.vanatta.helene.supplies.database.manage.ManageSiteDao;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for operations involving item updates at sites (item added to site, item removed from
 * site, item status changed).
 */
@Controller
@AllArgsConstructor
@Slf4j
public class ItemManagementController {

  private final Jdbi jdbi;
  private final SendSiteUpdate sendSiteUpdate;
  private final NewItemUpdate newItemUpdate;
  private final SendInventoryUpdate sendInventoryUpdate;
  private final DispatchRequestService dispatchRequestService;

  /** Returns null if ID is not valid or DNE. */
  String fetchSiteName(String siteId) {
    if (siteId == null || siteId.isBlank()) {
      return null;
    }

    try {
      long id = Long.parseLong(siteId);
      return ManageSiteDao.fetchSiteName(jdbi, id);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /** Creates a brand new item, and adds that item to a given site. */
  @PostMapping("/manage/add-site-item")
  @ResponseBody
  ResponseEntity<String> addNewSiteItem(@RequestBody Map<String, String> params) {
    String itemName = params.get("itemName");

    boolean itemAdded = ItemManagemenetDao.addNewItem(jdbi, itemName);
    if (!itemAdded) {
      log.warn("Failed to add item, already exists. Params: {}", params);
      return ResponseEntity.badRequest().body("Item not added, already exists");
    }
    newItemUpdate.sendNewItem(itemName);
    return updateSiteItemActive(params);
  }

  /** Adds an item to a site */
  @PostMapping("/manage/activate-site-item")
  @ResponseBody
  ResponseEntity<String> updateSiteItemActive(@RequestBody Map<String, String> params) {
    String siteId = params.get("siteId");
    String itemName = params.get("itemName");
    String itemStatus = params.get("itemStatus");
    log.info("Activating item: {}, siteId: {}, status: {}", itemName, siteId, itemStatus);
    if (siteId == null) {
      log.warn("Failed to activate item. No site id. Params: {}", params);
      return ResponseEntity.badRequest().body("Invalid site id, none specified.");
    }
    if (itemName == null) {
      log.warn("Failed to activate item. No item name. Params: {}", params);
      return ResponseEntity.badRequest().body("Invalid item name, none specified.");
    }
    if (itemStatus == null) {
      log.warn("Failed to activate item. No item name. No item status: {}", params);
      return ResponseEntity.badRequest().body("Invalid item status, none specified.");
    }

    String siteName = fetchSiteName(siteId);
    if (siteName == null) {
      log.warn("Failed to activate item. Invalid site id: {}, params: {}", siteId, params);
      return ResponseEntity.badRequest().body("Invalid site id");
    }

    if (!ItemStatus.allItemStatus().contains(itemStatus)) {
      log.warn("Failed to activate item. Invalid item status: {}, params: {}", itemStatus, params);
      return ResponseEntity.badRequest().body("Invalid item status: " + itemStatus);
    }

    ItemManagemenetDao.updateSiteItemActive(jdbi, Long.parseLong(siteId), itemName, itemStatus);

    new Thread(
            () -> {
              // send new item->site association to Make to external data sources
              sendInventoryUpdate.send(Long.parseLong(siteId));

              // adding item to site: if the item status is one of need, send a dispatching request
              var status = ItemStatus.fromTextValue(itemStatus);
              if (status.isNeeded()) {
                dispatchRequestService.addDispatch(
                    siteName, itemName, ItemStatus.fromTextValue(itemStatus));
              }
            })
        .start();

    return ResponseEntity.ok("Updated");
  }

  /** Removes an item from a site */
  @PostMapping("/manage/deactivate-site-item")
  @ResponseBody
  ResponseEntity<String> updateSiteItemInactive(@RequestBody Map<String, String> params) {
    String siteId = params.get("siteId");
    String itemName = params.get("itemName");
    if (siteId == null) {
      log.warn("Failed to deactivate item, no site id. Params: {}", params);
      throw new IllegalArgumentException("Invalid site id, none specified.");
    }
    if (itemName == null) {
      log.warn("Failed to deactivate item, no item name. Params: {}", params);
      throw new IllegalArgumentException("Invalid item name, none specified.");
    }

    log.info("Deactivating item: {}, siteId: {}", itemName, siteId);
    String siteName = fetchSiteName(siteId);
    if (siteName == null) {
      log.warn("Invalid site id: {}", siteId);
      return ResponseEntity.badRequest().body("Invalid site id");
    }

    ItemManagemenetDao.updateSiteItemInactive(jdbi, Long.parseLong(siteId), itemName);
    new Thread(
            () -> {
              sendInventoryUpdate.send(Long.parseLong(siteId));
              // removing item from site: send dispatch cancel
              dispatchRequestService.cancelDispatch(siteName, itemName);
            })
        .start();
    return ResponseEntity.ok("Updated");
  }

  /** Changes the status of an item within a site */
  @PostMapping("/manage/update-site-item-status")
  @ResponseBody
  ResponseEntity<String> updateSiteItemStatus(@RequestBody Map<String, String> params) {
    String siteId = params.get("siteId");
    String itemName = params.get("itemName");
    String newStatus = params.get("newStatus");

    log.info(
        "Updating item status, site id: {}, item name: {}, status: {}",
        siteId,
        itemName,
        newStatus);
    String siteName = fetchSiteName(siteId);
    if (siteName == null) {
      log.warn("Failed to update item status. Invalid site id: {}, params: {}", siteId, params);
      return ResponseEntity.badRequest().body("Invalid site id");
    }

    ItemStatus oldStatus =
        ItemManagemenetDao.fetchItemStatus(jdbi, Long.parseLong(siteId), itemName);

    if (oldStatus != ItemStatus.fromTextValue(newStatus)) {
      ItemManagemenetDao.updateItemStatus(jdbi, Long.parseLong(siteId), itemName, newStatus);
      new Thread(
              () -> {
                var latestStatus = ItemStatus.fromTextValue(newStatus);
                // if data is stale, or multiple browser windows, then the status
                // might not have actually changed. In which case, no-op.
                if (oldStatus != latestStatus) {
                  sendInventoryUpdate.send(Long.parseLong(siteId));

                  if (!oldStatus.isNeeded() && latestStatus.isNeeded()) {
                    // if status is moving to a status of need, then we need to do dispatch
                    dispatchRequestService.addDispatch(siteName, itemName, latestStatus);
                  } else if (oldStatus.isNeeded() && latestStatus.isNeeded()) {
                    // if old & latest status are needed, then this is a status change
                    dispatchRequestService.changePriority(siteName, itemName, latestStatus);
                  } else if (oldStatus.isNeeded() && !latestStatus.isNeeded()) {
                    // if latest status is not needed, then this is a cancel
                    dispatchRequestService.cancelDispatch(siteName, itemName);
                  }
                }
              })
          .start();
    }

    return ResponseEntity.ok("Updated");
  }
}
