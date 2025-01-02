package com.vanatta.helene.supplies.database.delivery;

import com.google.gson.Gson;
import jakarta.annotation.Nonnull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data object represents an update to a delivery from airtable. @see DeliveryUpdateWebhook */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryUpdate {
  @Nonnull Long deliveryId;

  /**
   * Code that is suffixed on the delivery manifest page, ie: /delivery/{publicUrlKey}<br>
   * The above URL should be used for this delivery.
   */
  String publicUrlKey;

  /**
   * Secret code for dispatchers to view delivery manifest page and have additional controls (eg:
   * send confirmation button).
   */
  String dispatcherCode;

  String deliveryStatus;
  List<String> dispatcherName;
  List<String> dispatcherNumber;
  List<String> driverName;
  List<String> driverNumber;
  List<Long> dropOffSiteWssId;
  List<Long> pickupSiteWssId;
  List<Long> itemListWssIds;
  List<String> itemList;
  List<String> licensePlateNumbers;
  String targetDeliveryDate;
  String dispatcherNotes;

  List<String> pickupSiteName;
  List<String> pickupContactName;
  List<String> pickupContactPhone;
  List<String> pickupHours;
  List<String> pickupAddress;
  List<String> pickupCity;
  List<String> pickupState;

  List<String> dropoffSiteName;
  List<String> dropoffContactName;
  List<String> dropoffContactPhone;
  List<String> dropoffHours;
  List<String> dropoffAddress;
  List<String> dropoffCity;
  List<String> dropoffState;

  static DeliveryUpdate parseJson(String inputJson) {
    return new Gson().fromJson(inputJson, DeliveryUpdate.class);
  }

  boolean isComplete() {
    return deliveryStatus != null && deliveryStatus.toLowerCase().contains("complete");
  }
}
