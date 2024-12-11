package com.vanatta.helene.supplies.database.delivery;

import com.google.gson.Gson;
import com.vanatta.helene.supplies.database.TestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryControllerTest {
  private static final String deliveryUpdateInput =
      """
     {
        "deliveryId" : 68,
        "deliveryStatus" : "Creating Dispatch",
        "dispatcherName" : [
           "Joe Doe"
        ],
        "dispatcherNumber" : [
           "919.111.1111"
        ],
        "driverName" : ["Jane Doe"],
        "driverNumber" : ["919.222.2222"],
        "dropOffSiteWssId" : [
           337
        ],
        "itemListWssIds" : [
           161,
           191,
           192,
           152
        ],
        "licensePlateNumbers" : "XYZ-123,ABC-333",
        "pickupSiteWssId" : [
           3088
        ],
        "targetDeliveryDate" : "2024-12-13"
     }
     """;
  
  @Test
  void canParseInput() {
    DeliveryController.DeliveryUpdate update = DeliveryController.DeliveryUpdate.parseJson(deliveryUpdateInput);
    assertThat(update.getDeliveryId()).isEqualTo(68L);
    assertThat(update.getDeliveryStatus()).isEqualTo("Creating Dispatch");
    assertThat(update.getDispatcherName()).containsExactly("Joe Doe");
    assertThat(update.getDispatcherNumber()).containsExactly("919.111.1111");
    assertThat(update.getDriverName()).containsExactly("Jane Doe");
    assertThat(update.getDriverNumber()).containsExactly("919.222.2222");
    assertThat(update.getDropOffSiteWssId()).containsExactly(337L);
    assertThat(update.getPickupSiteWssId()).containsExactly(3088L);
    assertThat(update.getItemListWssIds()).contains(161L, 191L, 192L, 152L);
    assertThat(update.getLicensePlateNumbers()).isEqualTo("XYZ-123,ABC-333");
    assertThat(update.getTargetDeliveryDate()).isEqualTo("2024-12-13");
  }
  
  // these values come from TestData.sql
  static final long SITE1_WSS_ID = -10;
  static final long SITE2_WSS_ID = -20;
  static final long WATER_WSS_ID = -40;
  static final long GLOVES_WSS_ID = -50;
  
  DeliveryController deliveryController = new DeliveryController();
  
  @Test
  void deliveriesStored() {
    DeliveryController.DeliveryUpdate update = DeliveryController.DeliveryUpdate.parseJson(deliveryUpdateInput);

    var inputData = update.toBuilder()
        .pickupSiteWssId(List.of(SITE1_WSS_ID))
        .dropOffSiteWssId(List.of(SITE2_WSS_ID))
        .itemListWssIds(List.of(WATER_WSS_ID, GLOVES_WSS_ID))
        .build();
    
    
    deliveryController.upsertDelivery(new Gson().toJson(inputData));
    
    
//    var deliveryData = DeliveryDao.lookupDeliveriesBySite("Site1");
  
    
    
  }

  
}
