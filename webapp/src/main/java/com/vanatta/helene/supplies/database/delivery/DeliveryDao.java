package com.vanatta.helene.supplies.database.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class DeliveryDao {
  
  // store
  
  
  // get
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  static class DeliveryData {
    long deliveryId;
    String deliveryStatus;
    String dispatcherName;
    String dispatcherNumber;
    String driverName;
    String driverNumber;
    String dropOffSiteName;
    Long dropOffSiteWssId;
    String pickupSiteName;
    Long pickupSiteWssId;
    List<String> itemNames;
    String licensePlateNumbers;
    String targetDeliveryDate;
  }
}
