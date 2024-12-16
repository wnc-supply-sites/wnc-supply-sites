package com.vanatta.helene.supplies.database.delivery;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DeliveryControllerTest {

  @BeforeAll
  static void setupDatabase() {
    TestConfiguration.setupDatabase();
  }

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
        "licensePlateNumbers" : ["XYZ-123,ABC-333"],
        "pickupSiteWssId" : [
           3088
        ],
        "targetDeliveryDate" : "2024-12-13",
        "dispatcherNotes": "notes from dispatcher"
     }
     """;

  String deliveryInput2 = """
  {"deliveryId":91,"itemListWssIds":[296],"driverNumber":[],"driverName":[],"dispatcherNumber":["828.279.2054"],"dispatcherName":["John"],"deliveryStatus":"Creating Dispatch","dropOffSiteWssId":[107],"pickupSiteWssId":[101],"targetDeliveryDate":null,"licensePlateNumbers":[]}
  """;
  // these values come from TestData.sql
  static final long SITE1_WSS_ID = -10;
  static final long SITE2_WSS_ID = -20;
  static final long WATER_WSS_ID = -40;
  static final long GLOVES_WSS_ID = -50;

  @Test
  void canParseInput() {
    DeliveryController.DeliveryUpdate update =
        DeliveryController.DeliveryUpdate.parseJson(deliveryUpdateInput);
    assertThat(update.getDeliveryId()).isEqualTo(68L);
    assertThat(update.getDeliveryStatus()).isEqualTo("Creating Dispatch");
    assertThat(update.getDispatcherName()).containsExactly("Joe Doe");
    assertThat(update.getDispatcherNumber()).containsExactly("919.111.1111");
    assertThat(update.getDriverName()).containsExactly("Jane Doe");
    assertThat(update.getDriverNumber()).containsExactly("919.222.2222");
    assertThat(update.getDropOffSiteWssId()).containsExactly(337L);
    assertThat(update.getPickupSiteWssId()).containsExactly(3088L);
    assertThat(update.getItemListWssIds()).contains(161L, 191L, 192L, 152L);
    assertThat(update.getLicensePlateNumbers()).containsExactly("XYZ-123,ABC-333");
    assertThat(update.getTargetDeliveryDate()).isEqualTo("2024-12-13");
    assertThat(update.getDispatcherNotes()).isEqualTo("notes from dispatcher");
  }

  @Test
  void canParse2() {
    DeliveryController.DeliveryUpdate.parseJson(deliveryInput2);
  }
  
  
  DeliveryController deliveryController = new DeliveryController(TestConfiguration.jdbiTest);

  @Test
  void deliveriesStored() {
    DeliveryController.DeliveryUpdate update =
        DeliveryController.DeliveryUpdate.parseJson(deliveryUpdateInput);

    var inputData =
        update.toBuilder()
            .pickupSiteWssId(List.of(SITE1_WSS_ID))
            .dropOffSiteWssId(List.of(SITE2_WSS_ID))
            .itemListWssIds(List.of(WATER_WSS_ID, GLOVES_WSS_ID))
            .build();

    assertThat(
            DeliveryDao.fetchDeliveriesBySiteId(
                TestConfiguration.jdbiTest, TestConfiguration.getSiteId("site1")))
        .isEmpty();

    var response = deliveryController.upsertDelivery(new Gson().toJson(inputData));
    assertThat(response.getStatusCode().value()).isEqualTo(200);

    var deliveries =
        DeliveryDao.fetchDeliveriesBySiteId(
            TestConfiguration.jdbiTest, TestConfiguration.getSiteId("site1"));
    assertThat(deliveries).hasSize(1);
    var delivery = deliveries.getFirst();
    assertThat(delivery.getDeliveryNumber()).isEqualTo(68L);
    assertThat(delivery.getDeliveryStatus()).isEqualTo("Creating Dispatch");
    assertThat(delivery.getDispatcherName()).isEqualTo("Joe Doe");
    assertThat(delivery.getDispatcherNumber()).isEqualTo("919.111.1111");
    assertThat(delivery.getDriverName()).isEqualTo("Jane Doe");
    assertThat(delivery.getDriverNumber()).isEqualTo("919.222.2222");
    assertThat(delivery.getFromSite()).isEqualTo("site1");
    assertThat(delivery.getToSite()).isEqualTo("site2");
    assertThat(delivery.getItemList()).contains("gloves", "water");
    assertThat(delivery.getDriverLicensePlate()).isEqualTo("XYZ-123,ABC-333");
    assertThat(delivery.getDeliveryDate()).isEqualTo("2024-12-13");

    // validate site lookup details
    var details =
        SiteDetailDao.lookupSiteById(
            TestConfiguration.jdbiTest, TestConfiguration.getSiteId("site1"));
    assertThat(delivery.getFromSite()).isEqualTo(details.getSiteName());
    assertThat(delivery.getFromAddress()).isEqualTo(details.getAddress());
    assertThat(delivery.getFromCity()).isEqualTo(details.getCity());
    assertThat(delivery.getFromState()).isEqualTo(details.getState());
    assertThat(delivery.getFromContactName()).isEqualTo(details.getContactName());
    assertThat(delivery.getFromContactPhone()).isEqualTo(details.getContactNumber());

    var toDetails =
        SiteDetailDao.lookupSiteById(
            TestConfiguration.jdbiTest, TestConfiguration.getSiteId("site2"));
    assertThat(delivery.getToSite()).isEqualTo(toDetails.getSiteName());
    assertThat(delivery.getToAddress()).isEqualTo(toDetails.getAddress());
    assertThat(delivery.getToCity()).isEqualTo(toDetails.getCity());
    assertThat(delivery.getToState()).isEqualTo(toDetails.getState());
    assertThat(delivery.getToContactName()).isEqualTo(toDetails.getContactName());
    assertThat(delivery.getToContactPhone()).isEqualTo(toDetails.getContactNumber());

    // now update the delivery that we just inserted
    var updatedInput =
        inputData.toBuilder()
            .deliveryStatus("In Progress")
            .dispatcherName(List.of("Jane Doe"))
            .dispatcherNumber(List.of("555.555.5555"))
            .driverName(List.of("driver"))
            .driverNumber(List.of("333.333.3333"))
            // we are flipping the to & from site IDs
            .pickupSiteWssId(List.of(SITE2_WSS_ID))
            .dropOffSiteWssId(List.of(SITE1_WSS_ID))
            .licensePlateNumbers(List.of("Traveler"))
            .targetDeliveryDate("2024-12-15")
            // we are reducing the number of items from 2 to 1
            .itemListWssIds(List.of(WATER_WSS_ID))
            .build();

    // now update the delivery that we just inserted
    response = deliveryController.upsertDelivery(new Gson().toJson(updatedInput));
    assertThat(response.getStatusCode().value()).isEqualTo(200);

    deliveries =
        DeliveryDao.fetchDeliveriesBySiteId(
            TestConfiguration.jdbiTest, TestConfiguration.getSiteId("site1"));
    assertThat(deliveries).hasSize(1);
    delivery = deliveries.getFirst();
    assertThat(delivery.getDeliveryNumber()).isEqualTo(68L);
    assertThat(delivery.getDeliveryStatus()).isEqualTo("In Progress");
    assertThat(delivery.getDispatcherName()).isEqualTo("Jane Doe");
    assertThat(delivery.getDispatcherNumber()).isEqualTo("555.555.5555");
    assertThat(delivery.getDriverName()).isEqualTo("driver");
    assertThat(delivery.getDriverNumber()).isEqualTo("333.333.3333");
    assertThat(delivery.getToSite()).isEqualTo("site1");
    assertThat(delivery.getFromSite()).isEqualTo("site2");
    assertThat(delivery.getItemList()).containsExactly("water");
    assertThat(delivery.getDriverLicensePlate()).isEqualTo("Traveler");
    assertThat(delivery.getDeliveryDate()).isEqualTo("2024-12-15");
  }

  /**
   * Fetch deliveries for site2, we should have at least one inserted from TestData.sql. Delete a
   * delivery, then assert there are one fewer deliveries.
   */
  @Test
  void deleteDelivery() {
    var deliveries =
        DeliveryDao.fetchDeliveriesBySiteId(
            TestConfiguration.jdbiTest, TestConfiguration.getSiteId("site2"));
    int deliveryCount = deliveries.size();
    assertThat(deliveryCount).isGreaterThan(0);
    long deliveryId = deliveries.getFirst().getDeliveryNumber();

    DeliveryDao.deleteDelivery(TestConfiguration.jdbiTest, deliveryId);

    deliveries =
        DeliveryDao.fetchDeliveriesBySiteId(
            TestConfiguration.jdbiTest, TestConfiguration.getSiteId("site2"));
    assertThat(deliveries).hasSize(deliveryCount - 1);
  }
}
