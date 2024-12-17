package com.vanatta.helene.supplies.database.delivery;

import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailController;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

// @Value
// @Builder
@ToString
@EqualsAndHashCode
@Getter
public class Delivery {
  private final long deliveryNumber;
  private final String deliveryStatus;

  /** link to the delivery detail page */
  private final String detailLink;

  private final String fromSite;
  private final String fromSiteLink;

  private final String toSite;
  private final String toSiteLink;
  private final String deliveryDate;

  private final String driverName;
  private final String driverNumber;
  private final String driverLicensePlate;

  private final String dispatcherName;
  private final String dispatcherNumber;
  private final String dispatcherNotes;

  private final List<String> itemList = new ArrayList<>();

  private final String fromAddress;
  private final String fromCity;
  private final String fromState;
  private final String fromContactName;
  private final String fromContactPhone;
  private final String fromHours;

  private final String toAddress;
  private final String toCity;
  private final String toState;
  private final String toContactName;
  private final String toContactPhone;
  private final String toHours;

  Delivery(DeliveryDao.DeliveryData dbData) {
    this.deliveryNumber = dbData.getDeliveryId();
    this.deliveryStatus = Optional.ofNullable(dbData.getDeliveryStatus()).orElse("Scheduling");
    this.detailLink = DeliveryController.buildDeliveryPageLink(dbData.getDeliveryId());
    this.deliveryDate = Optional.ofNullable(dbData.getTargetDeliveryDate()).orElse("Scheduling");
    this.driverName = dbData.getDriverName();
    this.driverNumber = dbData.getDriverNumber();
    this.driverLicensePlate = dbData.getLicensePlateNumbers();
    this.dispatcherName = dbData.getDispatcherName();
    this.dispatcherNumber = dbData.getDispatcherNumber();
    this.dispatcherNotes = dbData.getDispatcherNotes();
    this.fromSite = dbData.getFromSiteName();
    this.fromSiteLink = SiteDetailController.buildSiteLink(dbData.getFromSiteId());
    this.fromAddress = dbData.getFromAddress();
    this.fromCity = dbData.getFromCity();
    this.fromState = dbData.getFromState();
    this.fromContactName = dbData.getFromContactName();
    this.fromContactPhone = dbData.getFromContactPhone();
    this.fromHours = dbData.getFromHours();
    this.toSite = dbData.getToSiteName();
    this.toSiteLink = SiteDetailController.buildSiteLink(dbData.getToSiteId());
    this.toAddress = dbData.getToAddress();
    this.toCity = dbData.getToCity();
    this.toState = dbData.getToState();
    this.toContactName = dbData.getToContactName();
    this.toContactPhone = dbData.getToContactPhone();
    this.toHours = dbData.getToHours();
  }

  //  int itemCount;
  public int getItemCount() {
    return itemList.size();
  }

  String getItemListTruncated() {
    return getItemListTruncated(itemList);
  }

  // @VisibleForTesting
  static String getItemListTruncated(List<String> itemList) {
    if (itemList == null || itemList.isEmpty()) {
      return "";
    } else if (itemList.size() < 4) {
      return itemList.stream().sorted().collect(Collectors.joining(", "));
    } else {
      return String.join(", ", itemList.stream().sorted().toList().subList(0, 3)) + "...";
    }
  }
}
