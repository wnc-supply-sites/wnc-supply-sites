package com.vanatta.helene.supplies.database.manage.inventory;

import com.vanatta.helene.supplies.database.auth.LoggedInAdvice;
import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.export.update.SendInventoryUpdate;
import com.vanatta.helene.supplies.database.export.update.SendNewItemUpdate;
import com.vanatta.helene.supplies.database.manage.ManageSiteDao;
import com.vanatta.helene.supplies.database.manage.SelectSiteController;
import com.vanatta.helene.supplies.database.manage.UserSiteAuthorization;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
import com.vanatta.helene.supplies.database.util.ThreadRunner;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for operations involving item updates at sites (item added to site, item removed from
 * site, item status changed).
 */
@Controller
@Slf4j
public class InventoryController {
  public static final String PATH_INVENTORY = "/manage/inventory/inventory";

  public static String buildInventoryPath(long siteId) {
    return PATH_INVENTORY + "?siteId=" + siteId;
  }

  private final Jdbi jdbi;
  private final SendNewItemUpdate sendNewItemUpdate;
  private final SendInventoryUpdate sendInventoryUpdate;

  public InventoryController(
      Jdbi jdbi, SendNewItemUpdate sendNewItemUpdate, SendInventoryUpdate sendInventoryUpdate) {
    this.jdbi = jdbi;
    this.sendNewItemUpdate = sendNewItemUpdate;
    this.sendInventoryUpdate = sendInventoryUpdate;
  }

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

  /** Display inventory listing for a site. */
  @GetMapping(PATH_INVENTORY)
  ModelAndView fetchSiteInventoryListing(
      @ModelAttribute(LoggedInAdvice.USER_SITES) List<Long> sites, @RequestParam String siteId) {
    SiteDetailDao.SiteDetailData data =
        UserSiteAuthorization.isAuthorizedForSite(jdbi, sites, siteId).orElse(null);
    if (data == null) {
      return new ModelAndView("redirect:" + SelectSiteController.PATH_SELECT_SITE);
    }

    Map<String, Object> pageParams = new HashMap<>();
    pageParams.put("siteName", data.getSiteName());
    pageParams.put("siteId", siteId);

    List<ItemInventoryDisplay> inventoryList =
        ManageSiteDao.fetchSiteInventory(jdbi, Long.parseLong(siteId)).stream()
            .map(ItemInventoryDisplay::new)
            .sorted(
                Comparator.comparing(
                    d -> d.getItemName().toUpperCase())) // ItemInventoryDisplay::getItemName))
            .toList();

    pageParams.put("inventoryList", inventoryList);

    return new ModelAndView("manage/inventory/inventory", pageParams);
  }

  @Data
  @Builder
  @AllArgsConstructor
  static class ItemInventoryDisplay {
    String itemName;

    /** Should either be blank or "checked" */
    @Builder.Default String itemChecked = "";

    @Builder.Default String urgentChecked = "";
    @Builder.Default String neededChecked = "";
    @Builder.Default String availableChecked = "";
    @Builder.Default String oversupplyChecked = "";

    ItemInventoryDisplay(ManageSiteDao.SiteInventory siteInventory) {
      itemName = siteInventory.getItemName();
      itemChecked = siteInventory.isActive() ? "checked" : "";

      urgentChecked =
          ItemStatus.URGENTLY_NEEDED.getText().equalsIgnoreCase(siteInventory.getItemStatus())
              ? "checked"
              : "";
      neededChecked =
          ItemStatus.NEEDED.getText().equalsIgnoreCase(siteInventory.getItemStatus())
              ? "checked"
              : "";
      oversupplyChecked =
          ItemStatus.OVERSUPPLY.getText().equalsIgnoreCase(siteInventory.getItemStatus())
              ? "checked"
              : "";

      // if none of the statuses are checked, then check 'available' by default.
      availableChecked =
          (urgentChecked.isEmpty() && neededChecked.isEmpty() && oversupplyChecked.isEmpty())
              ? "checked"
              : "";
    }

    @SuppressWarnings("unused")
    public String getItemLabelClass() {
      if (urgentChecked != null && !urgentChecked.isEmpty()) {
        return ItemStatus.URGENTLY_NEEDED.getCssClass();
      } else if (neededChecked != null && !neededChecked.isEmpty()) {
        return ItemStatus.NEEDED.getCssClass();
      } else if (availableChecked != null && !availableChecked.isEmpty()) {
        return ItemStatus.AVAILABLE.getCssClass();
      } else if (oversupplyChecked != null && !oversupplyChecked.isEmpty()) {
        return ItemStatus.OVERSUPPLY.getCssClass();
      } else {
        return ItemStatus.AVAILABLE.getCssClass();
      }
    }

    @SuppressWarnings("unused")
    public String getItemStatusDisabled() {
      if (itemChecked == null || itemChecked.isEmpty()) {
        return "disabled";
      } else {
        return "";
      }
    }
  }

  /** Creates a brand new item, and adds that item to a given site. */
  @PostMapping("/manage/add-site-item")
  @ResponseBody
  ResponseEntity<String> addNewSiteItem(
      @ModelAttribute(LoggedInAdvice.USER_SITES) List<Long> sites,
      @RequestBody Map<String, String> params) {

    String itemName =
        Optional.ofNullable(params.get("itemName"))
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "addNewSiteItem:: missing item name in params: " + params))
            .trim();

    log.info("Creating brand new item: {}", params);
    boolean itemAdded = InventoryDao.addNewItem(jdbi, itemName);
    if (!itemAdded) {
      log.warn("Failed to add item, already exists. Params: {}", params);
      return ResponseEntity.badRequest().body("Item not added, already exists");
    }
    sendNewItemUpdate.sendNewItem(itemName);
    return updateSiteItemActive(sites, params);
  }

  /** Adds an item to a site */
  @PostMapping("/manage/activate-site-item")
  @ResponseBody
  ResponseEntity<String> updateSiteItemActive(
      @ModelAttribute(LoggedInAdvice.USER_SITES) List<Long> sites,
      @RequestBody Map<String, String> params) {
    String siteId = params.get("siteId");
    SiteDetailDao.SiteDetailData siteData =
        UserSiteAuthorization.isAuthorizedForSite(jdbi, sites, siteId).orElse(null);
    if (siteData == null) {
      return ResponseEntity.status(401).build();
    }

    String itemName =
        Optional.ofNullable(params.get("itemName"))
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Failed to activate item for site, item name missing in params: " + params))
            .trim();
    String itemStatus = params.get("itemStatus");
    log.info("Activating item: {}, siteId: {}, status: {}", itemName, siteId, itemStatus);
    if (siteId == null) {
      log.warn("Failed to activate item. No site id. Params: {}", params);
      return ResponseEntity.badRequest().body("Invalid site id, none specified.");
    }

    if (itemStatus == null) {
      log.warn("Failed to activate item. No item name. No item status: {}", params);
      return ResponseEntity.badRequest().body("Invalid item status, none specified.");
    }

    if (!ItemStatus.allItemStatus().contains(itemStatus)) {
      log.warn("Failed to activate item. Invalid item status: {}, params: {}", itemStatus, params);
      return ResponseEntity.badRequest().body("Invalid item status: " + itemStatus);
    }

    InventoryDao.updateSiteItemActive(jdbi, Long.parseLong(siteId), itemName, itemStatus);

    ThreadRunner.run(() -> sendInventoryUpdate.send(Long.parseLong(siteId), itemName));

    return ResponseEntity.ok("Updated");
  }

  /** Removes an item from a site */
  @PostMapping("/manage/deactivate-site-item")
  @ResponseBody
  ResponseEntity<String> updateSiteItemInactive(
      @ModelAttribute(LoggedInAdvice.USER_SITES) List<Long> sites,
      @RequestBody Map<String, String> params) {
    String siteId = params.get("siteId");
    SiteDetailDao.SiteDetailData siteData =
        UserSiteAuthorization.isAuthorizedForSite(jdbi, sites, siteId).orElse(null);
    if (siteData == null) {
      return ResponseEntity.status(401).build();
    }

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

    InventoryDao.getInventoryWssId(jdbi, Long.parseLong(siteId), itemName)
        .ifPresent(
            wssId ->
                ThreadRunner.run(
                    () ->
                        sendInventoryUpdate.sendItemRemoval(
                            itemName, siteData.getSiteName(), wssId)));
    InventoryDao.updateSiteItemInactive(jdbi, Long.parseLong(siteId), itemName);
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

    ItemStatus oldStatus = InventoryDao.fetchItemStatus(jdbi, Long.parseLong(siteId), itemName);

    if (oldStatus != ItemStatus.fromTextValue(newStatus)) {
      InventoryDao.updateItemStatus(jdbi, Long.parseLong(siteId), itemName, newStatus);
      var latestStatus = ItemStatus.fromTextValue(newStatus);
      if (oldStatus != latestStatus) {
        ThreadRunner.run(() -> sendInventoryUpdate.send(Long.parseLong(siteId), itemName));
      }
    }

    return ResponseEntity.ok("Updated");
  }
}
